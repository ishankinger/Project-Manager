package com.example.projectmanager.base

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ActivityBase2Binding

class Base2Activity : AppCompatActivity() {

    private lateinit var binding : ActivityBase2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_base2)

        val navController = this.findNavController(R.id.base2Container)
        val bottomNavigationView = binding.bottomNavigationView
        setupWithNavController(bottomNavigationView, navController)
    }

}