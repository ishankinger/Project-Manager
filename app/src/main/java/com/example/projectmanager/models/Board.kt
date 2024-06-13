package com.example.projectmanager.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// This is the local variable to store the information of the board for a particular user
@Parcelize
data class Board(
    var name : String = "",
    val image : String = "",
    val createdBy : String = "",
    val assignedTo : ArrayList<String> = ArrayList(),
    var documentId : String = ""
) : Parcelable