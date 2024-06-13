package com.example.projectmanager.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.example.projectmanager.R
import com.example.projectmanager.databinding.FragmentIntroBinding

class IntroFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentIntroBinding>(inflater,R.layout.fragment_intro,container,false)

        binding.introButtonSignIn.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_introFragment_to_signInFragment)
        }
        binding.introButtonSignUp.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_introFragment_to_signUpFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Hide the action bar
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

}