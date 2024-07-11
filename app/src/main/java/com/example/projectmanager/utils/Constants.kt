package com.example.projectmanager.utils

/**
 * Here all the important names are stores in constants variables
 * These names are used to update the firestore properties so can't afford any typo in these
 * That's why stored in const val
 */

object Constants {
    const val USERS : String = "users"
    const val NAME : String = "name"
    const val DESCRIPTION : String = "description"
    const val MOBILE : String = "mobile"
    const val IMAGE : String = "image"
    const val BOARD : String = "board"
    const val ASSIGNED_TO = "assignedTo"
    const val PICK_IMAGE_REQUEST_CODE = 2
    const val DOCUMENT_ID : String = "documentId"
    const val TASK_LIST : String = "taskList"
    const val ID : String = "id"
    const val EMAIL : String = "email"
    const val PROJECT_MANAGER_PREFERENCE = "ProjectManagerPrefs"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
    const val FCM_TOKEN = "fcmToken"
    const val FCM_KEY_TITLE : String = "title"
    const val FCM_KEY_MESSAGE : String = "message"

}