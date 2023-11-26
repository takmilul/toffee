package com.banglalink.toffee.notification

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.apiservice.SetFcmToken
import com.banglalink.toffee.data.database.entities.NotificationInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.repository.DrmLicenseRepository
import com.banglalink.toffee.data.repository.NotificationInfoRepository
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.HostUrlOverrideType.*
import com.banglalink.toffee.enums.NotificationType.*
import com.banglalink.toffee.extension.ifNotNullOrBlank
import com.banglalink.toffee.model.BubbleConfig
import com.banglalink.toffee.model.PlayerOverlayData
import com.banglalink.toffee.receiver.NotificationActionReceiver
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.util.CoilUtils
import com.banglalink.toffee.util.Log
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
class ToffeeNotificationService : FirebaseMessagingService() {
    
    private var notificationId = 1
    private val gson: Gson = Gson()
    private val TAG = "ToffeeMessagingService"
    @Inject lateinit var setFcmToken: SetFcmToken
    @Inject lateinit var mPref: SessionPreference
    @Inject lateinit var cacheManager: CacheManager
    @Inject lateinit var commonPreference: CommonPreference
    private val NOTIFICATION_CHANNEL_NAME = "Toffee Channel"
    @Inject lateinit var drmLicenseRepo: DrmLicenseRepository
    private val coroutineContext = Dispatchers.IO + SupervisorJob()
    private val coroutineScope = CoroutineScope(coroutineContext)
    @Inject lateinit var notificationInfoRepository: NotificationInfoRepository
    
    companion object {
        const val ROW_ID = "id"
        const val DISMISS = 300
        const val WATCH_NOW = 100
        const val WATCH_LATER = 200
        const val CONTENT_VIEW = 400
        const val PUB_SUB_ID = "pub-sub_id"
        const val ACTION_NAME = "action_name"
        const val RESOURCE_URL = "resource_url"
        const val NOTIFICATION_ID = "notification_id"
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        coroutineScope.launch {
            try {
                setFcmToken.execute(token)
            } catch (e: Exception) {
                Log.e(TAG, "onNewToken: ${e.message}")
            }
        }
        Log.i(TAG, "Token: $token")
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.i(TAG, "From: " + remoteMessage.from!!)
        
        val notificationBuilder = NotificationBuilder(remoteMessage)
        val data: Map<String, String> = notificationBuilder.data
        Log.i("NOT_", "onMessageReceived: $data")
        when (notificationBuilder.notificationType?.lowercase()) {
            OVERLAY.type -> {
                try {
                    gson.fromJson(remoteMessage.data["notificationText"]?.trimIndent(), PlayerOverlayData::class.java)
                        ?.let { mPref.playerOverlayLiveData.postValue(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "playerOverlay: ${e.message}")
                }
            }
            LOGOUT.type -> {
                kickOutUser(data)
            }
            DRM_LICENSE_RELEASE.type -> {
                coroutineScope.launch {
                    releaseAllLicense()
                }
            }
            CHANGE_URL.type -> {
                val shouldOverride = data["should_override"].equals("true")
                val overrideUrl = data["hls_override_url"] ?: ""
                mPref.shouldOverrideHlsHostUrl = shouldOverride
                mPref.overrideHlsHostUrl = overrideUrl
            }
            CHANGE_URL_EXTENDED.type -> {
                val shouldOverride = data["should_override"].equals("true")
                val overrideUrl = data["override_url"] ?: ""
                when(data["overrideType"]) {
                    HLS.type -> {
                        mPref.shouldOverrideHlsHostUrl = shouldOverride
                        mPref.overrideHlsHostUrl = overrideUrl
                    }
                    DRM.type -> {
                        mPref.shouldOverrideDrmHostUrl = shouldOverride
                        mPref.overrideDrmHostUrl = overrideUrl
                    }
                    NCG.type -> {
                        mPref.shouldOverrideNcgHostUrl = shouldOverride
                        mPref.overrideNcgHostUrl = overrideUrl
                    }
                    BASE.type -> {
                        mPref.shouldOverrideBaseUrl = shouldOverride
                        mPref.overrideBaseUrl = overrideUrl
                    }
                    IMAGE.type -> {
                        mPref.shouldOverrideImageHostUrl = shouldOverride
                        mPref.overrideImageHostUrl = overrideUrl
                    }
                }
            }
            CLEAR_CACHE.type -> {
                val route = data["apiRoute"]?.trim()
                route?.let {
                    if (it == "all") {
                        cacheManager.clearAllCache()
                    } else {
                        val apiRoutes = it.split(",")
                        apiRoutes.forEach { route ->
                            cacheManager.clearCacheByUrl(route.trim())
                        }
                    }
                }
            }
            BETA_USER_DETECTION.type -> {
                handleBetaNotification(notificationBuilder)
            }
            CONTENT_REFRESH.type -> {
                cacheManager.clearCacheByUrl(ApiRoutes.GET_HOME_FEED_VIDEOS)
                coroutineScope.launch {
                    notificationBuilder.build()
                }
            }
            BUBBLE_CONFIG.type -> {
                try {
                    val bubbleConfig = gson.fromJson(remoteMessage.data["bubbleConfig"]?.trimIndent(), BubbleConfig::class.java)
                    mPref.isBubbleActive = bubbleConfig.isFifaBubbleActive
                    mPref.startBubbleService.postValue(bubbleConfig.isFifaBubbleActive)
                    if (mPref.isBubbleActive) {
                        mPref.bubbleConfigLiveData.postValue(bubbleConfig)
                    }
                } catch (e: Exception) {
                    Log.e("Bubble_", "bubbleConfig: ${e.message}")
                }
            }
            else -> {
                coroutineScope.launch {
                    notificationBuilder.build()
                }
            }
        }
    }
    
    @SuppressLint("InlinedApi")
    private fun handleBetaNotification(notificationBuilder: NotificationBuilder) {
        try {
            val isBeta = mPref.betaVersionCodes?.split(",")?.contains(BuildConfig.VERSION_CODE.toString()) == true
            val content = notificationBuilder.data["message"]
            val betaVersionCode = notificationBuilder.data["betaVersionCode"]
            val isLocalBeta = betaVersionCode?.split(",")?.contains(BuildConfig.VERSION_CODE.toString()) == true
            if (isBeta && isLocalBeta) {
                coroutineScope.launch {
                    val intent = Intent(this@ToffeeNotificationService, NotificationActionReceiver::class.java).putExtras(
                        bundleOf(
                            PUB_SUB_ID to notificationBuilder.pubSubId
                        ))
                    val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    val builder = notificationBuilder.getNotificationBuilder(true)?.apply {
                        setContentText(content)
                        setContentIntent(pendingIntent)
                    }
                    builder?.let { showNotification(it.build()) }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
    }
    
    private suspend fun releaseAllLicense() {
        drmLicenseRepo.deleteAll()
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
    
    inner class NotificationBuilder(private val remoteMessage: RemoteMessage) {
        val data: Map<String, String> get() = remoteMessage.data
        private var imageDrawable: Drawable? = null
        private val button = data["button"]
        val pubSubId = data["notificationId"]
        private val playNowUrl = data["playNowUrl"]
        private val thumbnailUrl = data["thumbnail"]
        private val resourceUrl = data["resourceUrl"]
        val notificationType = data["notificationType"]
        private val watchLaterUrl = data["watchLaterUrl"]
        private val customerId = data["customerId"]?.ifBlank { mPref.customerId }?.toString()?.toInt() ?: mPref.customerId
        private val title = data["notificationHeader"] ?: remoteMessage.notification?.title
        private val content = data["notificationText"] ?: remoteMessage.notification?.body
        private val imageUrl = data["image"] ?: remoteMessage.notification?.imageUrl?.toString()
        private val sound: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.toffee_notificaiton_sound)
        private val notificationInfo = NotificationInfo(null, customerId, notificationType, pubSubId, 0, 0, title, content, null, thumbnailUrl, imageUrl, resourceUrl, playNowUrl, watchLaterUrl)
        
        private suspend fun insertIntoDB() = notificationInfoRepository.insert(notificationInfo)
        
        private suspend fun loadImageDrawable() {
            imageDrawable = imageUrl?.let { CoilUtils.coilExecuteGet(this@ToffeeNotificationService, it) }
        }
        
        private suspend fun getThumbnailImage(): Bitmap? {
            var thumbnailImage: Bitmap? = imageDrawable?.toBitmap(48, 48)
            if (!thumbnailUrl.isNullOrBlank()) {
                val thumbnailDrawable = CoilUtils.coilExecuteGet(this@ToffeeNotificationService, thumbnailUrl) ?: imageDrawable
                thumbnailImage = thumbnailDrawable?.toBitmap(48, 48)
            }
            return thumbnailImage
        }
        
        @SuppressLint("InlinedApi")
        private suspend fun getPendingIntent(hasActionButton: Boolean = false, isWatchNow: Boolean = false): PendingIntent {
            val isWatchLater = hasActionButton && !isWatchNow
            val intent = Intent(this@ToffeeNotificationService, if (isWatchLater) NotificationActionReceiver::class.java else HomeActivity::class.java).apply {
                /**if watchNow button is visible and playNowUrl is not empty then use playNowUrl.
                 * 
                 * if watchLater button is visible no url will be used with that button.
                 * 
                 * otherwise use resourceUrl
                 */
                val playingUrl = if (hasActionButton) {
                    if (isWatchNow) { if (!playNowUrl.isNullOrBlank()) playNowUrl else resourceUrl } else null
                } else {
                    resourceUrl
                }
                putExtras(bundleOf(
                    PUB_SUB_ID to pubSubId,
                    ROW_ID to insertIntoDB(),
                    RESOURCE_URL to playingUrl,
                    NOTIFICATION_ID to notificationId,
                    ACTION_NAME to if (!hasActionButton) CONTENT_VIEW else { if (isWatchNow) WATCH_NOW else WATCH_LATER }
                ))
                playingUrl?.ifNotNullOrBlank { data = Uri.parse(it) }
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val requestCode = if (hasActionButton) { if (isWatchNow) 1 else 2 } else 0 // watchNow = 1, watchLater = 2, else = 0
            
            return if (isWatchLater) {
                PendingIntent.getBroadcast(this@ToffeeNotificationService, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getActivity(this@ToffeeNotificationService, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }
        }
        
        suspend fun getNotificationBuilder(ignoreContentIntent: Boolean = false): NotificationCompat.Builder? {
            loadImageDrawable()
            pubSubId?.let {
                try {
                    notificationInfoRepository.getNotificationById(it.toInt())?.let {
                        return null
                    }
                } catch (e: Exception) {
                    return null
                }
            }
            notificationInfoRepository.getLastNotification()?.let {
                if (it.title == title && it.content == content && it.imageUrl == imageUrl && it.resourceUrl == resourceUrl) {
                    return null
                }
            }
            val notificationStyle = if (notificationType?.equals(LARGE.type, ignoreCase = true) == true) {
                NotificationCompat.BigPictureStyle().bigPicture(imageDrawable?.toBitmap())
            } else {
                NotificationCompat.BigTextStyle().bigText(content)
            }
            return NotificationCompat.Builder(this@ToffeeNotificationService, NOTIFICATION_CHANNEL_NAME).apply {
                setSound(sound)
                setContentTitle(title)
                setContentText(content)
                setStyle(notificationStyle)
                setAutoCancel(true)
                setSmallIcon(R.drawable.ic_notification)
                priority = NotificationCompat.PRIORITY_MAX
                getThumbnailImage()?.let { setLargeIcon(it) }
                color = ContextCompat.getColor(applicationContext, R.color.colorAccent2)
                if (!ignoreContentIntent) {
                    setContentIntent(getPendingIntent())
                }
//                if (button == "true") {
//                    addAction(android.R.drawable.ic_media_play, "Watch Now", getPendingIntent(hasActionButton = true, isWatchNow = true))
//                    addAction(android.R.drawable.ic_delete, "Watch Later", getPendingIntent(hasActionButton = true))
//                }
            }
        }
        
        suspend fun build() {
            getNotificationBuilder()?.let {
                PubSubMessageUtil.sendNotificationStatus(pubSubId, PUBSUBMessageStatus.DELIVERED)
                showNotification(it.build())
            }
        }
    }
    
    private fun showNotification(notification: Notification) {
        if (mPref.isNotificationEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Generic Notification"
                val description = "All kinds of generic notification of Toffee"
                val importance = NotificationManager.IMPORTANCE_HIGH
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
            notificationId++
        }
    }
}