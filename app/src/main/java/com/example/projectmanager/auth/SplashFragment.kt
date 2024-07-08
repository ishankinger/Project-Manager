package com.example.projectmanager.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.example.projectmanager.R
import com.example.projectmanager.base.Base2Activity
import com.example.projectmanager.databinding.FragmentSplashBinding
import com.example.projectmanager.firebase.FireStore

/**
 *  Fragment to show the splash screen when the app opens
 *  Used Handler function for post delay and fade animation to show the views of fragment
 */

class SplashFragment : Fragment() {

    private lateinit var binding : FragmentSplashBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_splash,container,false)

        // Handler function will post delay any process for some interval
        Handler().postDelayed({

            val currentUserId = FireStore().getCurrentUserID()

            // if already login then go to home page
            if(currentUserId.isNotEmpty()){
                val intent = Intent(context, Base2Activity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            // else navigate to signIn/signUp
            else{
                Navigation.findNavController(binding.root).navigate(R.id.action_splashFragment_to_introFragment)
            }
        },2500)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide the action bar
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        // fade in animation for splash screen
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        binding.projectImage.startAnimation(fadeIn)
        binding.projectName.startAnimation(fadeIn)
    }

}