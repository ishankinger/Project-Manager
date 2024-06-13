package com.example.projectmanager.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.example.projectmanager.R
import com.example.projectmanager.base.Base2Activity
import com.example.projectmanager.databinding.FragmentSplashBinding
import com.example.projectmanager.firebase.FireStore

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentSplashBinding>(inflater,R.layout.fragment_splash,container,false)

        // Handler function will post delay any process for some interval
        Handler().postDelayed({

            val currentUserId = FireStore().getCurrentUserID()

            if(currentUserId.isNotEmpty()){
                val intent = Intent(context, Base2Activity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            else{
                Navigation.findNavController(binding.root).navigate(R.id.action_splashFragment_to_introFragment)
            }
        },2000)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Hide the action bar
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

}