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
import androidx.core.os.bundleOf
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.data.database.entities.NotificationInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.repository.DrmLicenseRepository
import com.banglalink.toffee.data.repository.NotificationInfoRepository
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.NotificationType
import com.banglalink.toffee.model.PlayerOverlayData
import com.banglalink.toffee.receiver.NotificationActionReceiver
import com.banglalink.toffee.receiver.NotificationActionReceiver.Companion.ACTION_NAME
import com.banglalink.toffee.receiver.NotificationActionReceiver.Companion.CONTENT_VIEW
import com.banglalink.toffee.receiver.NotificationActionReceiver.Companion.NOTIFICATION_ID
import com.banglalink.toffee.receiver.NotificationActionReceiver.Companion.PUB_SUB_ID
import com.banglalink.toffee.receiver.NotificationActionReceiver.Companion.RESOURCE_URL
import com.banglalink.toffee.receiver.NotificationActionReceiver.Companion.ROW_ID
import com.banglalink.toffee.receiver.NotificationActionReceiver.Companion.WATCH_LATER
import com.banglalink.toffee.receiver.NotificationActionReceiver.Companion.WATCH_NOW
import com.banglalink.toffee.util.CoilUtils
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
    @Inject lateinit var cacheManager: CacheManager
    @Inject lateinit var commonPreference: CommonPreference
    private val NOTIFICATION_CHANNEL_NAME = "Toffee Channel"
    @Inject lateinit var drmLicenseRepo: DrmLicenseRepository
    private val coroutineContext = Dispatchers.IO + SupervisorJob()
    private val imageCoroutineScope = CoroutineScope(coroutineContext)
    @Inject lateinit var notificationInfoRepository: NotificationInfoRepository
    
    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.i(TAG, "Token: $s")
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.from!!)
        
        if (remoteMessage.data.isNullOrEmpty()) {
            imageCoroutineScope.launch {
                handleDefaultNotification(
                    remoteMessage.notification?.title,
                    remoteMessage.notification?.body,
                    remoteMessage.notification?.imageUrl
                )
            }
        } else {
            val data: Map<String, String> = remoteMessage.data
            Log.i("NOT_", "onMessageReceived: $data")
            when (data["notificationType"]!!.lowercase()) {
                NotificationType.OVERLAY.type -> {
                    try {
                        gson.fromJson(remoteMessage.data["notificationText"]?.trimIndent(), PlayerOverlayData::class.java)
                            ?.let { mPref.playerOverlayLiveData.postValue(it) }
                    } catch (e: Exception) {
                        Log.e(TAG, "playerOverlay: ${e.message}")
                    }
                }
                NotificationType.LOGOUT.type -> {
                    kickOutUser(data)
                }
                NotificationType.DRM_LICENSE_RELEASE.type -> {
                    imageCoroutineScope.launch {
                        releaseAllLicense()
                    }
                }
                NotificationType.CHANGE_URL.type -> {
                    changeHlsUrl(data)
                }
                NotificationType.CLEAR_CACHE.type -> {
                    val route = data["apiRoute"]?.trim()
                    route?.let {
                        if (route == "all") {
                            cacheManager.clearAllCache()
                        } else {
                            cacheManager.clearCacheByUrl(route)
                        }
                    }
                }
                NotificationType.BETA_USER_DETECTION.type -> {
                    handleBetaNotification(data)
                }
                NotificationType.CONTENT_REFRESH.type -> {
                    cacheManager.clearCacheByUrl(ApiRoutes.GET_HOME_FEED_VIDEOS)
                    imageCoroutineScope.launch { 
                        handleNotificationWithOutImage(data)
                    }
                }
                else -> {
                    prepareNotification(data)
                }
            }
        }
    }
    
    private fun handleBetaNotification(data: Map<String, String>) {
        try {
            val isBeta = mPref.betaVersionCodes?.split(",")?.contains(BuildConfig.VERSION_CODE.toString()) == true
            val notificationId = data["notificationId"]
            val title = data["title"]
            val message = data["message"]
            val betaVersionCode = data["betaVersionCode"]
            val isLocalBeta = betaVersionCode?.split(",")?.contains(BuildConfig.VERSION_CODE.toString()) == true
            if (isBeta && isLocalBeta) {
                val sound = Uri.parse("android.resource://" + packageName + "/" + R.raw.velbox_notificaiton)
                val intent = Intent("com.toffee.notification_receiver").putExtras(
                    bundleOf(
                        NOTIFICATION_ID to notificationId,
                    )
                )
                val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_NAME)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setAutoCancel(true)
                    .setSound(sound)
                    .setContentText(message)
                    .setContentTitle(title)
                    .setContentIntent(pendingIntent)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                showNotification(builder.build())
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
    }
    
    private suspend fun releaseAllLicense() {
        drmLicenseRepo.deleteAll()
    }
    
    private fun changeHlsUrl(notificationData: Map<String, String>) {
        try {
            mPref.shouldOverrideHlsUrl = notificationData["should_override"].equals("true")
            if (mPref.shouldOverrideHlsUrl && notificationData["url_id"].equals("1")) {
                mPref.setHlsOverrideUrl(notificationData["hls_override_url"])
            }
        } catch (e: Exception) {
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
                    mPref.customerId.toString(), commonPreference.deviceId, mPref.phoneNumber -> {
                        mPref.forceLogoutUserLiveData.postValue(true)
                    }
                }
            }
        } catch (e: Exception) {
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
                        } else {
                            handleNotificationWithImage(data)
                        }
                    }
                }
            }
            val id = data["notificationId"]
            PubSubMessageUtil.sendNotificationStatus(id, PUBSUBMessageStatus.DELIVERED)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
    }
    
    private suspend fun handleDefaultNotification(title: String?, content: String?, imageUrl: Uri? = null) {
        val notificationInfo = NotificationInfo(null, 0, "DefaultNotificationType", "DefaultNotificationId", 0, 0, title, content, null, null, null, null, null, null)
        val rowId = notificationInfoRepository.insert(notificationInfo)
        val sound = Uri.parse("android.resource://" + packageName + "/" + R.raw.velbox_notificaiton)
        
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_NAME)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setSound(sound)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
        
        val drawable = imageUrl?.let { CoilUtils.
        coilExecuteGet(this, it) }
        if (drawable != null)
            builder.setLargeIcon(drawable.toBitmap(48, 48))
        
        val intent = Intent("com.toffee.notification_receiver").putExtras(
            bundleOf(
                ROW_ID to rowId,
                NOTIFICATION_ID to notificationId,
                ACTION_NAME to CONTENT_VIEW
            )
        )
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(pendingIntent)
        showNotification(builder.build())
    }
    
    private suspend fun handleSmallImage(data: Map<String, String>) {
        val title = data["notificationHeader"]
        val content = data["notificationText"]
        val resourceUrl = data["resourceUrl"]
        val thumbnailUrl = data["thumbnail"]
        val customerId: Int = data["customerId"]?.toInt() ?: 0
        val notificationInfo = NotificationInfo(null, customerId, data["notificationType"], data["notificationId"], 0, 0, title, content, null, thumbnailUrl, null, resourceUrl)
        val rowId = notificationInfoRepository.insert(notificationInfo)
        val sound = Uri.parse("android.resource://" + packageName + "/" + R.raw.velbox_notificaiton)
        var thumbnailImage: Bitmap? = null
        if (!thumbnailUrl.isNullOrBlank()) {
            val thumbnailDrawable = CoilUtils.coilExecuteGet(this, thumbnailUrl)
            thumbnailImage = thumbnailDrawable?.toBitmap(48, 48)
        }
        
        val intent = resourceUrl?.let { Intent(Intent.ACTION_VIEW, Uri.parse(it)) } ?: Intent(Intent.ACTION_VIEW)
        intent.putExtras(bundleOf(ROW_ID to rowId))
        
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0 or PendingIntent.FLAG_IMMUTABLE)
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
        val notificationInfo = NotificationInfo(null, customerId, data["notificationType"], pubSubId, 0, 0, title, content, null, thumbnailUrl, imageUrl, resourceUrl, playNowUrl, watchLaterUrl)
        val rowId = notificationInfoRepository.insert(notificationInfo)
        
        val drawable = CoilUtils.coilExecuteGet(this, imageUrl)
        
        val image = drawable?.toBitmap()
        var thumbnailImage: Bitmap? = image
        if (!thumbnailUrl.isNullOrBlank()) {
            val thumbnailDrawable = CoilUtils.coilExecuteGet(this, thumbnailUrl) ?: drawable
            thumbnailImage = thumbnailDrawable?.toBitmap(48, 48)
        }
        
        val sound = Uri.parse("android.resource://" + packageName + "/" + R.raw.velbox_notificaiton)
        val intent = Intent("com.toffee.notification_receiver").putExtras(
            bundleOf(
                ROW_ID to rowId,
                PUB_SUB_ID to pubSubId,
                RESOURCE_URL to resourceUrl,
                NOTIFICATION_ID to notificationId,
                ACTION_NAME to CONTENT_VIEW
            )
        ).setClass(this, NotificationActionReceiver::class.java)
        
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
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
            val watchNowIntent = Intent("com.toffee.notification_receiver").putExtras(
                bundleOf(
                    PUB_SUB_ID to pubSubId,
                    ACTION_NAME to WATCH_NOW,
                    RESOURCE_URL to playNowUrl,
                    NOTIFICATION_ID to notificationId,
                )
            ).setClass(this, NotificationActionReceiver::class.java)
            
            val watchLaterIntent = Intent("com.toffee.notification_receiver").putExtras(
                bundleOf(
                    PUB_SUB_ID to pubSubId,
                    ACTION_NAME to WATCH_LATER,
                    NOTIFICATION_ID to notificationId,
                )
            ).setClass(this, NotificationActionReceiver::class.java)
            
            val watchNowPendingIntent = PendingIntent.getBroadcast(this, 1, watchNowIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_media_play, "Watch Now", watchNowPendingIntent)
            val watchLaterPendingIntent = PendingIntent.getBroadcast(applicationContext, 2, watchLaterIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_delete, "Watch Later", watchLaterPendingIntent)
        }
        showNotification(builder.build())
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
        val notificationInfo = NotificationInfo(null, customerId, data["notificationType"], pubSubId, 0, 0, title, content, null, thumbnailUrl, null, resourceUrl, playNowUrl, watchLaterUrl)
        val rowId = notificationInfoRepository.insert(notificationInfo)
        
        val intent = Intent("com.toffee.notification_receiver").putExtras(
            bundleOf(
                ROW_ID to rowId,
                PUB_SUB_ID to pubSubId,
                RESOURCE_URL to resourceUrl,
                NOTIFICATION_ID to notificationId,
                ACTION_NAME to CONTENT_VIEW
            )
        ).setClass(this, NotificationActionReceiver::class.java)
        
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
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
            val watchNowIntent = Intent("com.toffee.notification_receiver").putExtras(
                bundleOf(
                    PUB_SUB_ID to pubSubId,
                    ACTION_NAME to WATCH_NOW,
                    RESOURCE_URL to playNowUrl,
                    NOTIFICATION_ID to notificationId,
                )
            ).setClass(this, NotificationActionReceiver::class.java)
            
            val watchLaterIntent = Intent("com.toffee.notification_receiver").putExtras(
                bundleOf(
                    PUB_SUB_ID to pubSubId,
                    ACTION_NAME to WATCH_LATER,
                    NOTIFICATION_ID to notificationId,
                )
            ).setClass(this, NotificationActionReceiver::class.java)
            
            val watchNowPendingIntent = PendingIntent.getBroadcast(this, 1, watchNowIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_media_play, "Watch Now", watchNowPendingIntent)
            val watchLaterPendingIntent = PendingIntent.getBroadcast(applicationContext, 2, watchLaterIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_delete, "Watch Later", watchLaterPendingIntent)
        }
        showNotification(builder.build())
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
            } else {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(notificationId, notification)
            }
        }
        notificationId++
    }
}