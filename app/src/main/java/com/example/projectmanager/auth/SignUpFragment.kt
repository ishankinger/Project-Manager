package com.example.projectmanager.auth

import android.annotation.SuppressLint
import android.app.Dialog
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
import androidx.navigation.Navigation
import com.example.projectmanager.R
import com.example.projectmanager.databinding.FragmentSignUpBinding
import com.example.projectmanager.firebase.FireStore
import com.example.projectmanager.models.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpFragment : Fragment() {

    private lateinit var binding : FragmentSignUpBinding
    private lateinit var mProgressDialog : Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_sign_up,container,false)

        binding.signUpButton.setOnClickListener {
            registerUser()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Hide the action bar
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    // this is called in the fireStore class in userRegister function after storing the data in the cloud
    fun userRegisteredSuccess(){

        hideProgressDialog()

        Toast.makeText(
            context,
            "you have successfully registered the email address",
            Toast.LENGTH_LONG
        ).show()

        Navigation.findNavController(binding.root).navigate(R.id.action_signUpFragment_to_introFragment)
    }

    // function will be called when signUp button clicked and will initialise the task of signUp
    @SuppressLint("CutPasteId")
    private fun registerUser(){

        val name : String = binding.signUpName.text.toString().trim{it <= ' '}
        val email : String = binding.signUpEmail.text.toString().trim{it <= ' '}
        val password : String = binding.signUpPassword.text.toString().trim{it <= ' '}

        if(validateForm(name,email,password)){

            showProgressDialog(resources.getString(R.string.please_wait))

            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid,name,registeredEmail)
                        FireStore().registerUser(this,user)

                    } else {
                        hideProgressDialog()
                        binding.signUpEmail.setText("")
                        binding.signUpPassword.setText("")
                        binding.signUpName.setText("")
                        showErrorSnackBar("Error occurs Email already exists")
                    }
                }
        }
    }

    // function to check whether the email, name and password are filled in the text views or not
    private fun validateForm(name : String, email: String, password : String) : Boolean{
        when{
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Please enter a name")
                return false
            }
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter an email")
                return false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter a password")
                return false
            }
            else ->{
                if(password.length <= 6){
                    showErrorSnackBar("Password must be of at least 6 characters")
                    return false
                }

                val len = email.length
                if(len <= 4){
                    showErrorSnackBar("Email is not formatted properly")
                    return false
                }
                if(email[len-1] != 'm' && email[len-2] != 'o' && email[len-3] != 'c' && email[len-4] != '.') {
                    showErrorSnackBar("Email is not formatted properly")
                    return false
                }

                return true
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