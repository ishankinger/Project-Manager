package com.example.projectmanager.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// This data class will store the properties of a Board
@Parcelize
data class Board(
    val name : String = "",
    val image : String = "",
    val createdBy : String = "",
    val assignedTo : ArrayList<String> = ArrayList(),
    var documentId : String = "",
    var taskList : ArrayList<Task> = ArrayList()
) : Parcelable