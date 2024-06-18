package com.example.projectmanager.note

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projectmanager.roomdatabase.NoteDao

class NoteViewModelFactory(val app : Application, private val db : NoteDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass : Class<T>) : T{
        return NoteViewModel(db,app) as T
    }
}