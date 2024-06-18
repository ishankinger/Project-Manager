package com.example.projectmanager.note

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.projectmanager.models.Note
import com.example.projectmanager.roomdatabase.NoteDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteViewModel(private val database : NoteDao, application: Application) : AndroidViewModel(application){

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun addNote(note : Note) =
        uiScope.launch{
            withContext(Dispatchers.IO){
                database.insertNote(note)
            }
        }

    fun deleteNote(note : Note) =
        uiScope.launch{
            withContext(Dispatchers.IO){
                database.deleteNote(note)
            }
        }

    fun updateNote(note : Note) =
        uiScope.launch{
            withContext(Dispatchers.IO){
                database.updateNote(note)
            }
        }

    fun getAllNotes() = database.getAllNotes()

    fun searchNote(query : String?) = database.searchNotes(query)


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}