package com.banglalink.toffee.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.api.get
import com.banglalink.toffee.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ToffeeMessagingService:FirebaseMessagingService() {

    private val TAG = "ToffeeMessagingService"
    private val NOTIFICATION_CHANNEL_NAME = "Toffee Channel"
    private var notificationId = 27745


    val coroutineContext = Dispatchers.IO + SupervisorJob()
    val imageCoroutineScope = CoroutineScope(coroutineContext)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom()!!)

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Data payload: " + remoteMessage.data)
            prepareNotification(remoteMessage.getData())
        }
    }
    private fun prepareNotification(data: Map<String, String>) {
        try {
            val notificationType = data["notificationType"]

            when {
                notificationType!!.equals("SMALL", ignoreCase = true) -> {

                  handleSmallImage(data)

                }
                notificationType.equals("LARGE", ignoreCase = true) -> {
                    val imageUrl = data["image"]
                    if(imageUrl.isNullOrBlank())
                        return
                    imageCoroutineScope.launch {
                        handleNotificationWithImage(data)
                    }

                }
//                notificationType.equals("LOGOUT", ignoreCase = true) -> EventBus.getDefault()
//                    .post(MessageEvent(content, MessageEvent.NOTIFICATION_LOGOUT_EVENT))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun handleSmallImage(data: Map<String, String>){
        val title = data["notificationHeader"]
        val content = data["notificationText"]
        val resourceUrl = data["resourceUrl"]

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(resourceUrl)
        )
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_NAME)

        builder.setSmallIcon(R.drawable.ic_notification)

        // Set the intent that will fire when the user taps the notification_bar_ic.
        builder.setContentIntent(pendingIntent)

        builder.setAutoCancel(true)

        val sound =
            Uri.parse("android.resource://" + packageName + "/" + R.raw.velbox_notificaiton)
        builder.setSound(sound)
        builder.setContentTitle(title)
        builder.setContentText(content)

        showNotification(builder.build())

    }

    private suspend fun handleNotificationWithImage(data: Map<String, String>){
        val imageUrl = data["image"]
        val title = data["notificationHeader"]
        val content = data["notificationText"]
        val resourceUrl = data["resourceUrl"]

        val drawable = Coil.get(imageUrl!!)
        val image = drawable.toBitmap(100,100)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resourceUrl))
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_NAME)

        builder.setSmallIcon(R.drawable.ic_notification)
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)


        val sound =
            Uri.parse("android.resource://" + packageName + "/" + R.raw.velbox_notificaiton)
        builder.setSound(sound)
        builder.setLargeIcon(image)
        builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(image))
        builder.setContentTitle(title)
        builder.setContentText(content)
        builder.priority = NotificationCompat.PRIORITY_MAX

        showNotification(builder.build())
    }

    private fun showNotification(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Generic Notification"
            val description = "All kinds of generic notification of Toffee"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Toffee Notification", name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
            notificationManager?.notify(notificationId++, notification)
        } else {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId++, notification)
        }
    }
}