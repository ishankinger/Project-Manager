package com.example.projectmanager.boards

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
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.databinding.FragmentBoardsCreateBinding
import com.example.projectmanager.firebase.FireStore
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class BoardsCreateFragment : Fragment() {

    private lateinit var binding : FragmentBoardsCreateBinding
    private var mSelectedImageFileUri : Uri? = null
    private var mProfileImageURL : String = ""
    private lateinit var mProgressDialog : Dialog
    private var bottomNavigationView: BottomNavigationView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_boards_create,container,false)

        // Initialize the BottomNavigationView instance
        bottomNavigationView = activity?.findViewById(R.id.bottomNavigationView)

        // Hide the BottomNavigationView when the fragment is created
        hideBottomNavigationView()

        binding.backButtonCreateBoard.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_boardsCreateFragment_to_boardsFragment2)
        }

        binding.createBoardImage.setOnClickListener {
            showImageChooser()
        }

        binding.createBoardButton.setOnClickListener {

            if(mSelectedImageFileUri != null){
                uploadUserImage()
            }
            else{
                val name : String = binding.createBoardName.text.toString().trim{it <= ' '}
                if(name == ""){
                    showErrorSnackBar("Enter the name of the Board")
                }
                else{
                    showProgressDialog(resources.getString(R.string.please_wait))
                    FireStore().signInRegisteredUser(this)
                }
            }
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
                    .into(binding.createBoardImage)
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
                    .child("BOARD_IMAGE" + System.currentTimeMillis()
                            + "." + getFileExtension(mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!)

                .addOnSuccessListener {
                    it.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        mProfileImageURL = it.toString()
                        FireStore().signInRegisteredUser(this)
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


    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        Toast.makeText(context,"Board Created Successfully",Toast.LENGTH_SHORT).show()
        Navigation.findNavController(binding.root).navigate(R.id.action_boardsCreateFragment_to_boardsFragment2)
    }

    fun createBoard(mUser : User){
        val assignedUsersArrayList : ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(FireStore().getCurrentUserID())

        val name : String = binding.createBoardName.text.toString().trim{it <= ' '}

        val board = Board(name,mProfileImageURL,mUser.name,assignedUsersArrayList)

        FireStore().createBoard(this,board)

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

    // function to show snack Bar event
    private fun showErrorSnackBar(message: String){
        val snackBar = Snackbar.make(binding.root,message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        context?.let { ContextCompat.getColor(it,R.color.snackbar_error_color) }
            ?.let { snackBarView.setBackgroundColor(it) }
        snackBar.show()
    }

}