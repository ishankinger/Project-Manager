package com.example.projectmanager.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// This data class will store the property for selected member to cards
@Parcelize
data class SelectedMembers(
    val id : String = "",
    val image : String = ""
): Parcelable