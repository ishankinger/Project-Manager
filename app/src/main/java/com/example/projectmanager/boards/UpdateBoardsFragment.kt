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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.databinding.FragmentUpdateBoardsBinding
import com.example.projectmanager.firebase.FireStore
import com.example.projectmanager.models.Board
import com.example.projectmanager.utils.Constants
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

/**
 * Fragment to update the board details
 */
class UpdateBoardsFragment : Fragment() {

    private lateinit var binding : FragmentUpdateBoardsBinding
    private lateinit var mProgressDialog : Dialog
    private var mSelectedImageFileUri : Uri? = null
    private var mProfileImageURL : String = ""
    private lateinit var mBoardDetails : Board

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_update_boards,container,false)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Board Details"

        val args = UpdateBoardsFragmentArgs.fromBundle(requireArguments())
        mBoardDetails = args.boardDetails

        binding.updateBoardName.setText(mBoardDetails.name)

        binding.updateBoardDescription.setText(mBoardDetails.description)

        Glide.with(this)
            .load(mBoardDetails.image)
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.baseline_playlist_add_circle_24)
            .into(binding.updateBoardImage)


        binding.updateBoardImage.setOnClickListener{
            showImageChooser()
        }

        binding.updateBoardButton.setOnClickListener {

            if(mSelectedImageFileUri != null){
                uploadUserImage()
            }
            else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateBoardDetails()
            }
        }

        return binding.root
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
                    .into(binding.updateBoardImage)
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
                        updateBoardDetails()
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

    fun boardDetailsUpdatedSuccess(){
        hideProgressDialog()
        Toast.makeText(context,"Board Updated Successfully",Toast.LENGTH_SHORT).show()
        findNavController().navigate(UpdateBoardsFragmentDirections.actionUpdateBoardsFragmentToTasksFragment(mBoardDetails.documentId))
    }

    private fun updateBoardDetails(){

        val boardsDetailsHashMap: HashMap<String,Any> = HashMap()

        var anyChangesMade = false

        if(binding.updateBoardName.text.toString().isEmpty()){
            hideProgressDialog()
            showErrorSnackBar("Name missing")
            return
        }
        else if(binding.updateBoardDescription.text.toString().isEmpty()){
            hideProgressDialog()
            showErrorSnackBar("Board Description missing")
            return
        }

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mBoardDetails.image) {
            anyChangesMade = true
            boardsDetailsHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if (binding.updateBoardName.text.toString() != mBoardDetails.name) {
            anyChangesMade = true
            boardsDetailsHashMap[Constants.NAME] = binding.updateBoardName.text.toString()
        }

        if (binding.updateBoardDescription.text.toString() != mBoardDetails.description) {
            anyChangesMade = true
            boardsDetailsHashMap[Constants.DESCRIPTION] = binding.updateBoardDescription.text.toString()
        }

        if (anyChangesMade) {
            FireStore().updateBoardDetails(this,mBoardDetails.documentId, boardsDetailsHashMap)
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
    fun hideProgressDialog(){
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