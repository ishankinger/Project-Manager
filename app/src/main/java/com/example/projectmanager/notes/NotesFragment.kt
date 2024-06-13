package com.example.projectmanager.notes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.projectmanager.R
import com.example.projectmanager.databinding.FragmentNotesBinding

class NotesFragment : Fragment() {

    private lateinit var binding : FragmentNotesBinding
    private val viewModel : NotesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_notes,container,false)

        return binding.root
    }

}