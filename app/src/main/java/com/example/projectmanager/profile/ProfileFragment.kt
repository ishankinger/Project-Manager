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
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.base.BaseActivity
import com.example.projectmanager.databinding.FragmentProfileBinding
import com.example.projectmanager.firebase.FireStore
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

/**
 * Profile fragment shows the user details
 * User can select new photos, change their details and can update them
 * These updated details will be also updated in firestore database
 * And also user can sign out from here
 */


class ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding
    private lateinit var mUserDetails : User
    private lateinit var mProgressDialog : Dialog

    // variable storing the uri of the selected image
    private var mSelectedImageFileUri : Uri? = null

    // variable storing the url of the image which we will update after clicking the update button
    private var mProfileImageURL : String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile,container,false)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "My Profile"

        // calling this function to get the user details
        FireStore().signInRegisteredUser(this)

        // profile image clicked
        binding.profileImage.setOnClickListener{
            showImageChooser()
        }

        // update button clicked, if uri not null then first upload file to storage and then update
        binding.updateButton.setOnClickListener {

            if(mSelectedImageFileUri != null){
                uploadUserImage()
            }
            else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }

        // sign Out button clicked
        binding.signOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(context, BaseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        return binding.root
    }

    // this function is to load the present data on the views
    fun updateProfileUsersDetails(user : User){

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

            // getting the uri of image file as activity result
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

            // creating storage reference in url
            val sRef : StorageReference =
                FirebaseStorage.getInstance().reference
                    .child("USER_IMAGE" + System.currentTimeMillis()
                            + "." + getFileExtension(mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!)

                // on adding the image to storage we can call update Profile function
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
        Toast.makeText(context,"Update Successful",Toast.LENGTH_SHORT).show()
        binding.root.findNavController().popBackStack(R.id.boardsFragment2,false)
    }

    // function to update the user profile in fireStore cloud after the update button is clicked
    private fun updateUserProfileData() {

        // updation done using Hash maps, so creating hash maps for updated values
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
            Toast.makeText(context,"No changes made",Toast.LENGTH_SHORT).show()
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