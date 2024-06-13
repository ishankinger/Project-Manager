package com.example.projectmanager.notes

import android.util.Log
import androidx.lifecycle.ViewModel

class NotesViewModel : ViewModel() {

    init{
        Log.i("NotesViewModel","Notes View Model Created")
    }



    override fun onCleared() {
        super.onCleared()
        Log.i("NotesViewModel","Notes ViewModel Destroyed")
    }
}