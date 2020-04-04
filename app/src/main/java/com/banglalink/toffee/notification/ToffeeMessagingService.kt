package com.banglalink.toffee.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.api.get
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.receiver.NotificationActionReceiver
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ToffeeMessagingService:FirebaseMessagingService() {

    private val TAG = "ToffeeMessagingService"
    private val NOTIFICATION_CHANNEL_NAME = "Toffee Channel"
    private var notificationId = 1


    private val coroutineContext = Dispatchers.IO + SupervisorJob()
    private val imageCoroutineScope = CoroutineScope(coroutineContext)


    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.i(TAG, "Token: $s")
        Preference.getInstance().fcmToken = s
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.from!!)

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Data payload: " + remoteMessage.data)
            prepareNotification(remoteMessage.data)
        } else {
            Log.d(TAG, "Data payload empty" + remoteMessage.notification?.title )
            Log.d(TAG, "Data payload empty" + remoteMessage.notification?.body )

            imageCoroutineScope.launch {
                handleDefaultNotification(remoteMessage.notification?.title, remoteMessage.notification?.body, remoteMessage.notification?.imageUrl)
            }
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
            Log.e(TAG, e.message, e)
        }

    }


    private suspend fun handleDefaultNotification(
        title: String?,
        content: String?,
        imageUrl: Uri? = null
    ) {

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_NAME)
        builder.setSmallIcon(R.drawable.ic_notification)

        builder.setAutoCancel(true)

        val sound =
            Uri.parse("android.resource://" + packageName + "/" + R.raw.velbox_notificaiton)
        builder.setSound(sound)
        builder.setContentTitle(title)
        builder.setContentText(content)

        val drawable  = imageUrl?.let { Coil.get(it) }
        if (drawable != null)
            builder.setLargeIcon(drawable?.toBitmap(48, 48))

        var intent = Intent("com.toffee.notification_receiver")
        intent.putExtra(NotificationActionReceiver.NOTIFICATION_ID, notificationId)
        intent.putExtra(NotificationActionReceiver.ACTION_NAME, NotificationActionReceiver.DISMISS)

        var pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        builder.setContentIntent(pendingIntent)
        showNotification(builder.build())
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
        val thumbnailUrl = data["thumbnail"]
        val playNowUrl = data["playNowUrl"]
        val watchLaterUrl = data["watchLaterUrl"]
        val button = data["button"]


        val drawable = try{Coil.get(imageUrl!!)} catch (e: Exception) {null}

        val image = drawable?.toBitmap()

        var thumbnailImage: Bitmap? = image
        if  (!thumbnailUrl.isNullOrBlank()) {
            val thumbnailDrawable = try {
                Coil.get(thumbnailUrl)
            } catch (e: Exception) {drawable}
            thumbnailImage = thumbnailDrawable?.toBitmap(48,48)
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resourceUrl))
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_NAME)

        if (button == "true") {
            val watchLaterIntent = Intent("com.toffee.notification_receiver")
            watchLaterIntent.putExtra(NotificationActionReceiver.NOTIFICATION_ID, notificationId)
            watchLaterIntent.putExtra(NotificationActionReceiver.ACTION_NAME, NotificationActionReceiver.WATCH_LATER)
//            watchLaterIntent.putExtra(NotificationActionReceiver.ACTION_NAME, NotificationActionReceiver.WATCH_NOW)
            val watchLaterPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                0,
                watchLaterIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            builder.addAction(android.R.drawable.ic_delete, "Watch Later", watchLaterPendingIntent);


            val watchNowIntent = Intent("com.toffee.notification_receiver") //Intent(Intent.ACTION_VIEW, Uri.parse(playNowUrl))
            watchNowIntent.putExtra(NotificationActionReceiver.NOTIFICATION_ID, notificationId)
            watchNowIntent.putExtra(NotificationActionReceiver.ACTION_NAME, NotificationActionReceiver.WATCH_NOW)
            watchNowIntent.putExtra(NotificationActionReceiver.RESOURCE_URL, playNowUrl)
            val watchNowPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                0,
                watchNowIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            builder.addAction(android.R.drawable.ic_media_play, "Watch Now", watchNowPendingIntent);
        }

        builder.setSmallIcon(R.drawable.ic_notification)
        builder.color = ContextCompat.getColor(applicationContext, R.color.colorAccent2)
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)


        val sound =
            Uri.parse("android.resource://" + packageName + "/" + R.raw.velbox_notificaiton)
        builder.setSound(sound)
        builder.setLargeIcon(thumbnailImage)
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
            notificationManager?.notify(notificationId, notification)
        } else {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, notification)
        }
        notificationId++;
    }

}