package com.example.projectmanager.base

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ActivityBase2Binding

/**
 * Base2 Activity contains all fragments except the fragment for authentication
 * In this activity bottom navigation view is used to navigate between activities
 */

class Base2Activity : AppCompatActivity() {

    private lateinit var binding : ActivityBase2Binding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_base2)

        // Attaching the Nav Controller of Fragments to our Bottom Navigation View
        navController = this.findNavController(R.id.base2Container)
        val bottomNavigationView = binding.bottomNavigationView
        setupWithNavController(bottomNavigationView, navController)
    }

}