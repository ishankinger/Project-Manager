package com.example.projectmanager.base

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ActivityBaseBinding

/**
 * Base Activity containing fragments for authentication
 * After authentication we will navigate to Base2 Activity
 */

class BaseActivity : AppCompatActivity() {

    private lateinit var binding : ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_base)

    }

}