package com.example.projectmanager.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


// This data class will store all the properties of any task in board
@Parcelize
data class Task(
    var title : String = "",
    val createdBy : String = "",
    var cards : ArrayList<Card> = ArrayList()
) : Parcelable