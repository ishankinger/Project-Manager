package com.example.projectmanager.auth

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.projectmanager.R
import com.example.projectmanager.base.Base2Activity
import com.example.projectmanager.databinding.FragmentSignInBinding
import com.example.projectmanager.firebase.FireStore
import com.example.projectmanager.models.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class SignInFragment : Fragment() {

    private lateinit var binding : FragmentSignInBinding
    private lateinit var mProgressDialog : Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_sign_in,container,false)

        binding.signInButton.setOnClickListener {
            signInUser()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Hide the action bar
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    // This function is called after the end of sign in process
    // We will get the user information from the signInRegisteredUser function of FireStore class
    fun signInSuccess(user : User){

        hideProgressDialog()

        Toast.makeText(
            context,
            "Hi ${user.name}, Welcome to Project Manager",
            Toast.LENGTH_LONG
        ).show()

        val intent = Intent(context, Base2Activity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    // function will be called when signIn button clicked and will initialise the task of signIn
    @SuppressLint("CutPasteId")
    private fun signInUser(){

        val email : String = binding.signInEmail.text.toString().trim{it <= ' '}
        val password : String = binding.signInPassword.text.toString().trim{it <= ' '}

        if(validateForm(email,password)){

            showProgressDialog(resources.getString(R.string.please_wait))

            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email,password)
                .addOnCompleteListener {task->
                    if(task.isSuccessful){
                        FireStore().signInRegisteredUser(this)
                    }
                    else{
                        hideProgressDialog()
                        binding.signInEmail.setText("")
                        binding.signInPassword.setText("")
                        showErrorSnackBar("Authentication failed no such id found")
                    }
                }
        }
    }

    // function to check whether the email and password are filled in the text views or not
    private fun validateForm(email: String, password : String) : Boolean{
        return when{
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter an email")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter a password")
                false
            }
            else ->{
                true
            }
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

    // function to show snack Bar event
    private fun showErrorSnackBar(message: String){
        val snackBar = Snackbar.make(binding.root,message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        context?.let { ContextCompat.getColor(it,R.color.snackbar_error_color) }
            ?.let { snackBarView.setBackgroundColor(it) }
        snackBar.show()
    }

}