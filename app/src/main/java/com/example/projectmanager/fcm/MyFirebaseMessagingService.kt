package com.example.projectmanager.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.projectmanager.R
import com.example.projectmanager.base.Base2Activity
import com.example.projectmanager.base.BaseActivity
import com.example.projectmanager.firebase.FireStore
import com.example.projectmanager.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object{
        private const val TAG = "MyFirebaseMsgService"
    }

    // Data messages received here and notification messages are only received here when app is in foreground
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG,"FROM : ${remoteMessage.from}")

        remoteMessage.data.isNotEmpty().let{
            Log.d(TAG,"MESSAGE DATA PAYLOAD : ${remoteMessage.from}")
            val title = remoteMessage.data[Constants.FCM_KEY_TITLE]!!
            val message = remoteMessage.data[Constants.FCM_KEY_MESSAGE]!!
            sendNotification(title,message)
        }

        remoteMessage.notification?.let{
            Log.d(TAG,"MESSAGE NOTIFICATION BODY : ${it.body}")
            sendNotification(remoteMessage.notification!!.title!!,remoteMessage.notification!!.body!!)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG,"Refresh token : $token")
        sendRegistration(token)
    }

    private fun sendRegistration(token : String?){
        // Implement functionality
    }

    private fun sendNotification(title : String, message : String){
        val intent = if(FireStore().getCurrentUserID().isNotEmpty()){
            Intent(this,Base2Activity::class.java)
        }else{
            Intent(this,BaseActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
        Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this,0,intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = this.resources.getString(R.string.default_notification_channel_id)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(
            this,channelId)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelId,
                "Channel Progject Manager title",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0,notificationBuilder.build())
    }
}