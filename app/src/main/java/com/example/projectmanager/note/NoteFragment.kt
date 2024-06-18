package com.example.projectmanager.note

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.projectmanager.R
import com.example.projectmanager.databinding.FragmentNoteBinding
import com.example.projectmanager.roomdatabase.NoteDatabase

class NoteFragment : Fragment() {

    private lateinit var binding : FragmentNoteBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_note, container,false)

        val application = requireNotNull(this.activity).application

        val dataSource = NoteDatabase.getInstance(application).noteDao

        val noteViewModelFactory = NoteViewModelFactory(application,dataSource)

        val noteViewModel = ViewModelProvider(this, noteViewModelFactory)[NoteViewModel::class.java]

        binding.noteViewModel = noteViewModel

        binding.setLifecycleOwner(this)

        return binding.root
    }
}