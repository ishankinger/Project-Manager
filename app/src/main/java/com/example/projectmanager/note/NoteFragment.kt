package com.example.projectmanager.note

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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

        val adapter = NoteAdapter(NoteAdapter.NoteClickListener {
            Toast.makeText(context,"Note was clicked",Toast.LENGTH_SHORT).show()
        })

        binding.noteRecyclerView.adapter = adapter

        val manager = LinearLayoutManager(activity)
        binding.noteRecyclerView.layoutManager = manager

        noteViewModel.noteList.observe(viewLifecycleOwner) {
            it?.let {
                if(it.isNotEmpty()){
                    adapter.submitList(it)
                    binding.noteRecyclerView.visibility = View.VISIBLE
                    binding.emptyNotesImage.visibility = View.GONE
                }
                else{
                    binding.noteRecyclerView.visibility = View.GONE
                    binding.emptyNotesImage.visibility = View.VISIBLE
                }
            }
        }

        return binding.root
    }
}