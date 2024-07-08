package com.example.projectmanager.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// This data class will store the property of the card in taskList of a board
@Parcelize
data class Card(
    val name : String = "",
    val createdBy : String = "",
    val assignedTo : ArrayList<String> = ArrayList(),
    val labelColor : String = "",
    val dueDate : Long = 0
) : Parcelable