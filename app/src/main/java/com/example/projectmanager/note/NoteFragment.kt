package com.example.projectmanager.note

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectmanager.R
import com.example.projectmanager.databinding.FragmentNoteBinding
import com.example.projectmanager.models.Note
import com.example.projectmanager.roomdatabase.NoteDatabase
import com.google.android.material.bottomsheet.BottomSheetDialog

class NoteFragment : Fragment(), SearchView.OnQueryTextListener, MenuProvider {

    private lateinit var binding : FragmentNoteBinding
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_note, container,false)

        val application = requireNotNull(this.activity).application

        val dataSource = NoteDatabase.getInstance(application).noteDao

        val noteViewModelFactory = NoteViewModelFactory(application,dataSource)

        noteViewModel = ViewModelProvider(this, noteViewModelFactory)[NoteViewModel::class.java]

        binding.noteViewModel = noteViewModel

        binding.setLifecycleOwner(this)

        val menuHost : MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)

        noteAdapter = NoteAdapter(NoteAdapter.NoteClickListener { note->
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.list_note_edit, null)
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()
            view.findViewById<EditText>(R.id.editNoteTitle).setText(note.noteTitle)
            view.findViewById<EditText>(R.id.editNoteDesc).setText(note.noteDesc)

            val autoId = note.id
            val actionButton: Button = view.findViewById(R.id.saveNoteButton)
            actionButton.setOnClickListener {
                val noteTitle = view.findViewById<EditText>(R.id.editNoteTitle).text.toString().trim()
                val noteDesc = view.findViewById<EditText>(R.id.editNoteDesc).text.toString().trim()
                if(noteTitle.isNotEmpty()){
                    val upadateNote = Note(autoId,noteTitle,noteDesc)
                    noteViewModel.updateNote(upadateNote)
                    bottomSheetDialog.dismiss()
                }
                else{
                    Toast.makeText(context,"Enter note title",Toast.LENGTH_SHORT).show()
                }
            }

            view.findViewById<Button>(R.id.deleteNoteButton).setOnClickListener{
                noteViewModel.deleteNote(note)
                Toast.makeText(context,"Note Deleted",Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }
        })

        binding.noteRecyclerView.adapter = noteAdapter

        val manager = LinearLayoutManager(activity)
        binding.noteRecyclerView.layoutManager = manager

        noteViewModel.noteList.observe(viewLifecycleOwner) {
            it?.let {
                if(it.isNotEmpty()){
                    noteAdapter.submitList(it)
                    binding.noteRecyclerView.visibility = View.VISIBLE
                    binding.emptyNotesImage.visibility = View.GONE
                }
                else{
                    binding.noteRecyclerView.visibility = View.GONE
                    binding.emptyNotesImage.visibility = View.VISIBLE
                }
            }
        }

        binding.addNoteFab.setOnClickListener{
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.list_note_add, null)
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()

            val actionButton: Button = view.findViewById(R.id.addNoteButton)
            actionButton.setOnClickListener {
                val noteTitle = view.findViewById<EditText>(R.id.addNoteTitle).text.toString().trim()
                val noteDesc = view.findViewById<EditText>(R.id.addNoteDesc).text.toString().trim()
                if(noteTitle.isNotEmpty()){
                    val note = Note(0,noteTitle,noteDesc)
                    noteViewModel.addNote(note)
                    bottomSheetDialog.dismiss()
                }
                else{
                    Toast.makeText(context,"Enter note title",Toast.LENGTH_SHORT).show()
                }
            }
        }

        return binding.root
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if(query != null){
            searchNote(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if(newText != null){
            searchNote(newText)
        }
        return true
    }

    private fun searchNote(query : String?){
        val searchQuery = "%$query"
        noteViewModel.searchNote(searchQuery).observe(this){ list->
            noteAdapter.submitList(list)
        }
    }


    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.note_search_menu,menu)

        val menuSearch = menu.findItem(R.id.searchMenu).actionView as SearchView
        menuSearch.isSubmitButtonEnabled = false
        menuSearch.setOnQueryTextListener(this)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }
}