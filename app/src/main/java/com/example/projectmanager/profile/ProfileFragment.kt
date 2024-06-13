package com.example.projectmanager.profile

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.base.BaseActivity
import com.example.projectmanager.databinding.FragmentProfileBinding
import com.example.projectmanager.firebase.FireStore
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException


class ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding
    private lateinit var mUserDetails : User
    private var bottomNavigationView: BottomNavigationView? = null


    // variable storing the uri of the selected image
    private var mSelectedImageFileUri : Uri? = null

    // variable storing the url of the image which we will update after clicking the update button
    private var mProfileImageURL : String = ""

    private lateinit var mProgressDialog : Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile,container,false)

        showProgressDialog(" ")
        FireStore().signInRegisteredUser(this)

        // Initialize the BottomNavigationView instance
        bottomNavigationView = activity?.findViewById(R.id.bottomNavigationView)

        // Hide the BottomNavigationView when the fragment is created
        hideBottomNavigationView()

        binding.backButtonProfile.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_profileFragment2_to_boardsFragment2)
        }

        binding.profileImage.setOnClickListener{
//            Toast.makeText(context,"clicked",Toast.LENGTH_LONG).show()
//            if(ContextCompat.checkSelfPermission(
//                    requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
//                == PackageManager.PERMISSION_GRANTED){
//                showImageChooser()
//            }
//            else{
//                ActivityCompat.requestPermissions(
//                    requireActivity(),
//                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
//                    Constants.READ_STORAGE_PERMISSION_CODE
//                )
//            }
            showImageChooser()
        }

        binding.updateButton.setOnClickListener {

            if(mSelectedImageFileUri != null){
                uploadUserImage()
            }
            else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }

        binding.signOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(context, BaseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        return binding.root
    }


    override fun onResume() {
        super.onResume()
        // Hide the BottomNavigationView again when the fragment is resumed
        hideBottomNavigationView()
    }

    override fun onPause() {
        super.onPause()
        // Show the BottomNavigationView when the fragment is paused
        showBottomNavigationView()
    }

    private fun hideBottomNavigationView() {
        bottomNavigationView?.visibility = View.GONE
    }

    private fun showBottomNavigationView() {
        bottomNavigationView?.visibility = View.VISIBLE
    }


    // this function is to load the present data on the views
    fun updateProfileUsersDetails(user : User){
        hideProgressDialog()

        mUserDetails = user

        binding.profileEmail.setText(user.email)
        binding.profileName.setText(user.name)
        binding.profileMobileNumber.setText(user.mobile.toString())

        Glide.with(this)
            .load(user.image)
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.profileImage)

    }

    // function to get the permission result
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty()){
                showImageChooser()
            }
            else{
                Toast.makeText(
                    context,
                    "Oops, you just denied the permission for storage.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Function to choose image from our device after permission is granted
    private fun showImageChooser(){
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, Constants.PICK_IMAGE_REQUEST_CODE)
    }

    // Generic code for updating the image on the image view after choosing image from device
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null){

            mSelectedImageFileUri = data.data

            try{
                Glide.with(this)
                    .load(mSelectedImageFileUri.toString())
                    .centerCrop()
                    .circleCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.profileImage)
            }catch(e: IOException){
                e.printStackTrace()
            }
        }
    }

    // function to upload image to the firebase storage
    private fun uploadUserImage(){

        showProgressDialog(resources.getString(R.string.please_wait))

        if(mSelectedImageFileUri != null){

            val sRef : StorageReference =
                FirebaseStorage.getInstance().reference
                    .child("USER_IMAGE" + System.currentTimeMillis()
                            + "." + getFileExtension(mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!)

                .addOnSuccessListener {
                    it.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        mProfileImageURL = it.toString()
                        updateUserProfileData()
                    }
                }
                .addOnFailureListener{
                    Toast.makeText(context, "file not loaded to the firebase storage", Toast.LENGTH_LONG).show()
                }
        }
    }

    // used in upload User image function to give it a name
    private fun getFileExtension(uri : Uri?) : String?{
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(requireActivity().contentResolver.getType(uri!!))
    }

    // last function called when all update process is overed
    fun profileUpdateSuccess(){
        hideProgressDialog()
        Toast.makeText(context,"Profile Updated",Toast.LENGTH_SHORT).show()
    }

    // function to update the user profile in fireStore cloud after the update button is clicked
    private fun updateUserProfileData() {
        val userHashMap: HashMap<String,Any> = HashMap()
        var anyChangesMade = false

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            anyChangesMade = true
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if (binding.profileName.text.toString() != mUserDetails.name) {
            anyChangesMade = true
            userHashMap[Constants.NAME] = binding.profileName.text.toString()
        }

        if (binding.profileMobileNumber.text.toString() != mUserDetails.mobile.toString()) {
            anyChangesMade = true
            userHashMap[Constants.MOBILE] = binding.profileMobileNumber.text.toString().toLong()
        }

        if (anyChangesMade) {
            FireStore().updateUserProfileData(this, userHashMap)
        }
        else{
            hideProgressDialog()
        }
    }

    // function to show progress dialog box when some task is going on
    private fun showProgressDialog(text : String){
        mProgressDialog = context?.let { Dialog(it) }!!
        mProgressDialog.setContentView(R.layout.dialog_progress)
        mProgressDialog.findViewById<TextView>(R.id.progressBarText).text = text
        mProgressDialog.setCancelable(false)
        mProgressDialog.show()
    }

    // this will stop showing dialog box when long running task is completed
    private fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }

}