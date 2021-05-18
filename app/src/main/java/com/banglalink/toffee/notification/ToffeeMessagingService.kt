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
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.entities.NotificationInfo
import com.banglalink.toffee.data.repository.NotificationInfoRepository
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.NotificationType
import com.banglalink.toffee.model.PlayerOverlayData
import com.banglalink.toffee.receiver.NotificationActionReceiver
import com.banglalink.toffee.util.UtilsKt
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ToffeeMessagingService : FirebaseMessagingService() {

    private var notificationId = 1
    private val gson: Gson = Gson()
    private val TAG = "ToffeeMessagingService"
    @Inject lateinit var mPref: SessionPreference
    @Inject lateinit var commonPreference: CommonPreference
    private val NOTIFICATION_CHANNEL_NAME = "Toffee Channel"
    @Inject lateinit var notificationInfoRepository: NotificationInfoRepository
    private val coroutineContext = Dispatchers.IO + SupervisorJob()
    private val imageCoroutineScope = CoroutineScope(coroutineContext)
    
    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.i(TAG, "Token: $s")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.from!!)
        
        if (remoteMessage.data.isNullOrEmpty()){
            imageCoroutineScope.launch {
                handleDefaultNotification(
                    remoteMessage.notification?.title,
                    remoteMessage.notification?.body,
                    remoteMessage.notification?.imageUrl
                )
            }
        }
        else {
            val data: Map<String, String> = remoteMessage.data
            Log.i("NOT_", "onMessageReceived: $data")
            when(data["notificationType"]!!.toLowerCase()) {
                NotificationType.OVERLAY.type -> {
                    gson.fromJson(remoteMessage.data["notificationText"]?.trimIndent(), PlayerOverlayData::class.java)?.let { mPref.playerOverlayLiveData.postValue(it) }
                }
                NotificationType.LOGOUT.type -> {
                    kickOutUser(data)
                }
                NotificationType.CHANGE_URL.type -> {
                    changeHlsUrl(data)
                }
                else -> {
                    prepareNotification(data)
                }
            }
        }
    }
    
    private fun changeHlsUrl(notificationData: Map<String, String>) {
        try {
            mPref.shouldOverrideHlsUrl = notificationData["should_override"].equals("true")
            if (mPref.shouldOverrideHlsUrl && notificationData["url_id"].equals("1")) {
                mPref.setHlsOverrideUrl(notificationData["hls_override_url"])
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "changeHlsUrl: ${e.message}")
        }
    }
    
    private fun kickOutUser(data: Map<String, String>) {
        try {
            val authList: Array<String>? = Gson().fromJson(data["message"]?.trimIndent(), Array<String>::class.java)
            authList?.forEach { value ->
                var decryptedData = Base64.decode(value, Base64.DEFAULT)
                repeat(2) { decryptedData = Base64.decode(decryptedData, Base64.DEFAULT) }
                when (String(decryptedData)) {
                    mPref.customerId.toString() -> {
                        mPref.forceLogoutUserLiveData.postValue(true)
                        return
                    }
                    commonPreference.deviceId -> {
                        mPref.forceLogoutUserLiveData.postValue(true)
                        return
                    }
                    mPref.phoneNumber -> {
                        mPref.forceLogoutUserLiveData.postValue(true)
                        return
                    }
                }
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "kickOutUser: ${e.message}")
        }
    }
    
    private fun prepareNotification(data: Map<String, String>) {
        try {
            val notificationType = data["notificationType"]

            when {
                notificationType!!.equals("SMALL", ignoreCase = true) -> {
                    imageCoroutineScope.launch {
                        handleSmallImage(data)
                    }
                }
                notificationType.equals("LARGE", ignoreCase = true) -> {
                    imageCoroutineScope.launch {
                        val imageUrl = data["image"]
                        if (imageUrl.isNullOrBlank()) {
                            handleNotificationWithOutImage(data)
                        }
                        else {
                            handleNotificationWithImage(data)
                        }
                    }
                }
//                notificationType.equals("LOGOUT", ignoreCase = true) -> EventBus.getDefault()
//                    .post(MessageEvent(content, MessageEvent.NOTIFICATION_LOGOUT_EVENT))
            }

            //sending pub-sub message
            val id = data["notificationId"]
            //Message Status Meaning : /*0=Delivered,1=open, 2=later */
            PubSubMessageUtil.sendNotificationStatus(id, PUBSUBMessageStatus.DELIVERED)

        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
    }
    
    private suspend fun handleDefaultNotification(
        title: String?,
        content: String?,
        imageUrl: Uri? = null
    ) {

        val sound =
            Uri.parse("android.resource://" + packageName + "/" + R.raw.velbox_notificaiton)

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_NAME)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setSound(sound)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))

        val drawable = imageUrl?.let { UtilsKt.coilExecuteGet(this, it) }
        if (drawable != null)
            builder.setLargeIcon(drawable.toBitmap(48, 48))

        val intent = Intent("com.toffee.notification_receiver")
        intent.putExtra(NotificationActionReceiver.NOTIFICATION_ID, notificationId)
        intent.putExtra(
            NotificationActionReceiver.ACTION_NAME,
            NotificationActionReceiver.DISMISS
        )

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        builder.setContentIntent(pendingIntent)
        showNotification(builder.build())

        val notificationInfo = NotificationInfo(null, 0,  "DefaultNotificationType", "DefaultNotificationId", 0, 0, title, content, null, null, null, null, null, null)
        notificationInfoRepository.insert(notificationInfo)
    }

    private suspend fun handleSmallImage(data: Map<String, String>) {
        val title = data["notificationHeader"]
        val content = data["notificationText"]
        val resourceUrl = data["resourceUrl"]
        val thumbnailUrl = data["thumbnail"]
        val customerId: Int = data["customerId"]?.toInt() ?: 0

        val sound =
            Uri.parse("android.resource://" + packageName + "/" + R.raw.velbox_notificaiton)

        var thumbnailImage: Bitmap? = null
        if (!thumbnailUrl.isNullOrBlank()) {
            val thumbnailDrawable = try {
                UtilsKt.coilExecuteGet(this, thumbnailUrl)
            } catch (e: Exception) {
                null
            }
            thumbnailImage = thumbnailDrawable?.toBitmap(48, 48)
        }
        
        val intent = resourceUrl?.let { Intent(Intent.ACTION_VIEW, Uri.parse(it)) } ?: Intent(Intent.ACTION_VIEW)
        
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_NAME)

            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(sound)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(content)
            )

        if (thumbnailImage != null) {
            builder.setLargeIcon(thumbnailImage)
        }

        showNotification(builder.build())

        val notificationInfo = NotificationInfo(null, customerId, data["notificationType"], data["notificationId"], 0, 0, title, content, null, thumbnailUrl, null, resourceUrl)
        notificationInfoRepository.insert(notificationInfo)
    }

    private suspend fun handleNotificationWithImage(data: Map<String, String>) {
        val imageUrl = data["image"]
        val title = data["notificationHeader"]
        val content = data["notificationText"]
        val resourceUrl = data["resourceUrl"]
        val thumbnailUrl = data["thumbnail"]
        val playNowUrl = data["playNowUrl"]
        val watchLaterUrl = data["watchLaterUrl"]
        val button = data["button"]
        val pubSubId = data["notificationId"]
        val customerId: Int = data["customerId"]?.toInt() ?: 0

        val drawable = try {
            UtilsKt.coilExecuteGet(this, imageUrl)
        } catch (e: Exception) {
            null
        }

        val image = drawable?.toBitmap()

        var thumbnailImage: Bitmap? = image
        if (!thumbnailUrl.isNullOrBlank()) {
            val thumbnailDrawable = try {
                UtilsKt.coilExecuteGet(this, thumbnailUrl)
            } catch (e: Exception) {
                drawable
            }
            thumbnailImage = thumbnailDrawable?.toBitmap(48, 48)
        }

        val sound =
            Uri.parse("android.resource://" + packageName + "/" + R.raw.velbox_notificaiton)

//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resourceUrl))
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val intent = Intent("com.toffee.notification_receiver")
        intent.setClass(this, NotificationActionReceiver::class.java)
        intent.putExtra(NotificationActionReceiver.NOTIFICATION_ID, notificationId)
        intent.putExtra(NotificationActionReceiver.PUB_SUB_ID, pubSubId)
        intent.putExtra(NotificationActionReceiver.RESOURCE_URL, resourceUrl)
        intent.putExtra(
            NotificationActionReceiver.ACTION_NAME,
            NotificationActionReceiver.CONTENT_VIEW
        )
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_NAME)

        builder.setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(sound)
            .setLargeIcon(thumbnailImage)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(image))
            .setContentTitle(title)
            .setContentText(content)
            .priority = NotificationCompat.PRIORITY_MAX
        builder.color = ContextCompat.getColor(applicationContext, R.color.colorAccent2)

        if (button == "true") {

            val watchNowIntent = Intent("com.toffee.notification_receiver")
            watchNowIntent.setClass(this, NotificationActionReceiver::class.java)
            watchNowIntent.putExtra(NotificationActionReceiver.NOTIFICATION_ID, notificationId)
            watchNowIntent.putExtra(NotificationActionReceiver.PUB_SUB_ID, pubSubId)
            watchNowIntent.putExtra(
                NotificationActionReceiver.ACTION_NAME,
                NotificationActionReceiver.WATCH_NOW
            )
            watchNowIntent.putExtra(NotificationActionReceiver.RESOURCE_URL, playNowUrl)
            val watchNowPendingIntent = PendingIntent.getBroadcast(
                this,
                1,
                watchNowIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            builder.addAction(
                android.R.drawable.ic_media_play,
                "Watch Now",
                watchNowPendingIntent
            )

            val watchLaterIntent = Intent("com.toffee.notification_receiver")
            watchLaterIntent.setClass(this, NotificationActionReceiver::class.java)
            watchLaterIntent.putExtra(
                NotificationActionReceiver.NOTIFICATION_ID,
                notificationId
            )
            watchLaterIntent.putExtra(NotificationActionReceiver.PUB_SUB_ID, pubSubId)
            watchLaterIntent.putExtra(
                NotificationActionReceiver.ACTION_NAME,
                NotificationActionReceiver.WATCH_LATER
            )
            val watchLaterPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                2,
                watchLaterIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            builder.addAction(
                android.R.drawable.ic_delete,
                "Watch Later",
                watchLaterPendingIntent
            )
        }
        showNotification(builder.build())
        
        val notificationInfo = NotificationInfo(null, customerId, data["notificationType"], pubSubId, 0, 0, title, content, null, thumbnailUrl, imageUrl, resourceUrl, playNowUrl, watchLaterUrl)
        notificationInfoRepository.insert(notificationInfo)
    }

    private suspend fun handleNotificationWithOutImage(data: Map<String, String>) {
        val title = data["notificationHeader"]
        val content = data["notificationText"]
        val resourceUrl = data["resourceUrl"]
        val thumbnailUrl = data["thumbnail"]
        val playNowUrl = data["playNowUrl"]
        val watchLaterUrl = data["watchLaterUrl"]
        val button = data["button"]
        val pubSubId = data["notificationId"]
        val sound = Uri.parse("android.resource://" + packageName + "/" + R.raw.velbox_notificaiton)
        val customerId: Int = data["customerId"]?.toInt() ?: 0

        val intent = Intent("com.toffee.notification_receiver")
        intent.setClass(this, NotificationActionReceiver::class.java)
        intent.putExtra(NotificationActionReceiver.NOTIFICATION_ID, notificationId)
        intent.putExtra(NotificationActionReceiver.PUB_SUB_ID, pubSubId)
        intent.putExtra(NotificationActionReceiver.RESOURCE_URL, resourceUrl)
        intent.putExtra(
            NotificationActionReceiver.ACTION_NAME,
            NotificationActionReceiver.CONTENT_VIEW
        )
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_NAME)

        builder.setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(sound)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setContentTitle(title)
            .setContentText(content)
            .priority = NotificationCompat.PRIORITY_MAX
        builder.color = ContextCompat.getColor(applicationContext, R.color.colorAccent2)

        if (button == "true") {
            val watchNowIntent = Intent("com.toffee.notification_receiver")
            watchNowIntent.setClass(this, NotificationActionReceiver::class.java)
            watchNowIntent.putExtra(NotificationActionReceiver.NOTIFICATION_ID, notificationId)
            watchNowIntent.putExtra(NotificationActionReceiver.PUB_SUB_ID, pubSubId)
            watchNowIntent.putExtra(
                NotificationActionReceiver.ACTION_NAME,
                NotificationActionReceiver.WATCH_NOW
            )
            watchNowIntent.putExtra(NotificationActionReceiver.RESOURCE_URL, playNowUrl)
            val watchNowPendingIntent = PendingIntent.getBroadcast(
                this,
                1,
                watchNowIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            builder.addAction(
                android.R.drawable.ic_media_play,
                "Watch Now",
                watchNowPendingIntent
            )

            val watchLaterIntent = Intent("com.toffee.notification_receiver")
            watchLaterIntent.setClass(this, NotificationActionReceiver::class.java)
            watchLaterIntent.putExtra(
                NotificationActionReceiver.NOTIFICATION_ID,
                notificationId
            )
            watchLaterIntent.putExtra(NotificationActionReceiver.PUB_SUB_ID, pubSubId)
            watchLaterIntent.putExtra(
                NotificationActionReceiver.ACTION_NAME,
                NotificationActionReceiver.WATCH_LATER
            )
            val watchLaterPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                2,
                watchLaterIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            builder.addAction(
                android.R.drawable.ic_delete,
                "Watch Later",
                watchLaterPendingIntent
            )
        }
        showNotification(builder.build())
        
        val notificationInfo = NotificationInfo(null, customerId, data["notificationType"], pubSubId, 0, 0, title, content, null, thumbnailUrl, null, resourceUrl,
            playNowUrl, watchLaterUrl)
        notificationInfoRepository.insert(notificationInfo)
    }

    private fun showNotification(notification: Notification) {
        if (mPref.isNotificationEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Generic Notification"
                val description = "All kinds of generic notification of Toffee"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(NOTIFICATION_CHANNEL_NAME, name, importance)
                channel.description = description
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager?.createNotificationChannel(channel)
                notificationManager?.notify(notificationId, notification)
            }
            else {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(notificationId, notification)
            }
        }
        notificationId ++
    }
}