package com.banglalink.toffee.data.storage

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.extension.doIfNotNullOrEmpty
import com.banglalink.toffee.extension.isNotNullBlank
import com.banglalink.toffee.model.*
import com.banglalink.toffee.util.EncryptionUtil
import com.banglalink.toffee.util.SingleLiveEvent
import com.banglalink.toffee.util.Utils
import java.text.ParseException
import java.util.*

const val PREF_NAME_IP_TV = "IP_TV"

class SessionPreference(private val pref: SharedPreferences, private val context: Context) {
    
    val homeIntent = MutableLiveData<Intent?>()
    val viewCountDbUrlLiveData = SingleLiveEvent<String>()
    val reactionStatusDbUrlLiveData = SingleLiveEvent<String>()
    val subscriberStatusDbUrlLiveData = SingleLiveEvent<String>()
    val shareCountDbUrlLiveData = SingleLiveEvent<String>()
    val sessionTokenLiveData = MutableLiveData<String>()
    val profileImageUrlLiveData = MutableLiveData<String>()
    val splashConfigLiveData = MutableLiveData<List<DecorationData>?>()
    val customerNameLiveData = MutableLiveData<String>()
    val playerOverlayLiveData = SingleLiveEvent<PlayerOverlayData>()
    val forceLogoutUserLiveData = SingleLiveEvent<Boolean>()
    val loginDialogLiveData = SingleLiveEvent<Boolean>()
    val deleteDialogLiveData = SingleLiveEvent<Boolean>()
    val messageDialogLiveData = SingleLiveEvent<String>()
    val shareableUrlLiveData = SingleLiveEvent<String>()
    val isWebViewDialogOpened = SingleLiveEvent<Boolean>()
    val isWebViewDialogClosed = SingleLiveEvent<Boolean>()
    val isFireworkInitialized = MutableLiveData<Boolean>()
    val featuredPartnerIdLiveData = MutableLiveData(0)
    val bubbleVisibilityLiveData = SingleLiveEvent<Boolean>()
    val bubbleConfigLiveData = MutableLiveData<BubbleConfig?>()
    val nativeAdSettings = MutableLiveData<List<NativeAdSettings>?>()
    val shareableHashLiveData = MutableLiveData<Pair<String?, String?>>().apply { value = Pair(null, null) }
    val vastTagListV3LiveData = MutableLiveData<List<VastTagV3>?>()
    val categoryId = MutableLiveData<Int>()
    val categoryName = MutableLiveData<String>()
    val isCatWiseLinChannelAvailable = MutableLiveData<Boolean>()
    val startBubbleService = MutableLiveData<Boolean>()
    val isShowMoreToggled = SingleLiveEvent<Boolean>()
    val postLoginEventAction = SingleLiveEvent<(()->Unit)?>()
    val preLoginDestinationId = SingleLiveEvent<Int?>()
    val doActionBeforeReload = MutableLiveData<Boolean>()
    val activePremiumPackList = MutableLiveData<List<ActivePack>?>()
    
    var phoneNumber: String
        get() = pref.getString(PREF_PHONE_NUMBER, "") ?: ""
        set(phoneNumber) = pref.edit { putString(PREF_PHONE_NUMBER, phoneNumber) }
    
    var hePhoneNumber: String
        get() = pref.getString(PREF_HE_PHONE_NUMBER, "") ?: ""
        set(phoneNumber) = pref.edit { putString(PREF_HE_PHONE_NUMBER, phoneNumber) }
    
    var customerName: String
        get() = pref.getString(PREF_CUSTOMER_NAME, "") ?: ""
        set(customerName) {
            customerNameLiveData.postValue(customerName)
            pref.edit { putString(PREF_CUSTOMER_NAME, customerName) }
        }
    
    var customerEmail: String
        get() = pref.getString(PREF_CUSTOMER_EMAIL, "") ?: ""
        set(email) = pref.edit { putString(PREF_CUSTOMER_EMAIL, email) }
    
    var customerAddress: String
        get() = pref.getString(PREF_CUSTOMER_ADDRESS, "") ?: ""
        set(address) = pref.edit { putString(PREF_CUSTOMER_ADDRESS, address) }
    
    var customerDOB: String
        get() = pref.getString(PREF_CUSTOMER_DOB, "") ?: ""
        set(dob) = pref.edit { putString(PREF_CUSTOMER_DOB, dob) }
    
    var customerNID: String
        get() = pref.getString(PREF_CUSTOMER_NID, "") ?: ""
        set(nidNumber) = pref.edit { putString(PREF_CUSTOMER_NID, nidNumber) }
    
    var customerId: Int
        get() = pref.getInt(PREF_CUSTOMER_ID, 0)
        set(customerId) {
            pref.edit { putInt(PREF_CUSTOMER_ID, customerId) }
        }
    
    var password: String
        get() = pref.getString(PREF_PASSWORD, "") ?: ""
        set(password) {
            pref.edit().putString(PREF_PASSWORD, password).apply()
        }
    
    var sessionToken: String
        get() = pref.getString(PREF_SESSION_TOKEN, "") ?: ""
        set(sessionToken) {
            val storedToken = pref.getString(PREF_SESSION_TOKEN, "") ?: ""//get stored token
            pref.edit().putString(PREF_SESSION_TOKEN, sessionToken).apply()//save new session token
            if (storedToken.isNotEmpty() && !sessionToken.equals(storedToken, true)) {
                pref.edit().putLong(PREF_DEVICE_TIME_IN_MILLISECONDS, System.currentTimeMillis())
                    .apply()//Update session token change time
                sessionTokenLiveData.postValue(sessionToken)//post if there is mismatch of session token
            }
        }
    
    var isBanglalinkNumber: String
        get() = pref.getString(PREF_BANGLALINK_NUMBER, "false") ?: "false"
        set(isBanglalinkNumber) {
            pref.edit().putString(PREF_BANGLALINK_NUMBER, isBanglalinkNumber).apply()
        }
    
    var isHeBanglalinkNumber: Boolean
        get() = pref.getBoolean(PREF_HE_BANGLALINK_NUMBER, false)
        set(isVerified) {
            pref.edit().putBoolean(PREF_HE_BANGLALINK_NUMBER, isVerified).apply()
        }
    
    var isVerifiedUser: Boolean
        get() = pref.getBoolean(PREF_VERIFICATION, false)
        set(isVerified) {
            pref.edit().putBoolean(PREF_VERIFICATION, isVerified).apply()
        }
    
    var balance: Int
        get() = pref.getInt(PREF_BALANCE, 0)
        set(balance) {
            pref.edit().putInt(PREF_BALANCE, balance).apply()
        }
    
    var latitude: String
        get() = pref.getString(PREF_LATITUDE, "") ?: ""
        set(latitude) {
            pref.edit().putString(PREF_LATITUDE, latitude).apply()
        }
    
    var longitude: String
        get() = pref.getString(PREF_LONGITUDE, "") ?: ""
        set(Longitude) {
            pref.edit().putString(PREF_LONGITUDE, Longitude).apply()
        }
    
    var fcmToken: String
        get() = pref.getString(PREF_FCM_TOKEN, "") ?: ""
        set(token) {
            pref.edit().putString(PREF_FCM_TOKEN, token).apply()
        }
    
    var userImageUrl: String?
        get() = pref.getString(PREF_IMAGE_URL, null)
        set(userPhoto) {
            pref.edit().putString(PREF_IMAGE_URL, userPhoto).apply()
            if (!TextUtils.isEmpty(userPhoto)) profileImageUrlLiveData.postValue(userPhoto!!)
        }
    
    val netType: String
        get() = Utils.checkWifiOnAndConnected(context.applicationContext)
    
    var isSubscriptionActive: String
        get() = "false"
        set(phoneNumber) {
            pref.edit().putString(PREF_SUBSCRIPTION_ACTIVE, phoneNumber).apply()
        }
    
    var isFeaturePartnerActive: Boolean
        get() = pref.getBoolean(PREF_FEATURE_PARTNER_ACTIVE, false)
        set(isActive) {
            pref.edit().putBoolean(PREF_FEATURE_PARTNER_ACTIVE, isActive).apply()
        }
    
    fun getFireworkUserId(): String {
        var userId = pref.getString(PREF_FIREWORK_USER_ID, null)
        if (userId.isNullOrBlank()) {
            userId = "${UUID.randomUUID()}_${System.nanoTime()}"
            pref.edit().putString(PREF_FIREWORK_USER_ID, userId).apply()
        }
        return userId
    }
    
    var channelId: Int
        get() = pref.getInt(PREF_CHANNEL_ID, 0)
        set(channelId) = pref.edit().putInt(PREF_CHANNEL_ID, channelId).apply()
    
    var channelLogo: String
        get() = pref.getString(PREF_CHANNEL_LOGO, "") ?: ""
        set(channelLogoUrl) = pref.edit().putString(PREF_CHANNEL_LOGO, channelLogoUrl).apply()
    
    var channelName: String
        get() = pref.getString(PREF_CHANNEL_NAME, "") ?: ""
        set(channelName) = pref.edit().putString(PREF_CHANNEL_NAME, channelName).apply()
    
    var isChannelDetailChecked: Boolean
        get() = pref.getBoolean(PREF_IS_CHANNEL_DETAIL_CHECKED, false)
        set(value) = pref.edit().putBoolean(PREF_IS_CHANNEL_DETAIL_CHECKED, value).apply()
    
    fun setSystemTime(systemTime: String) {
        pref.edit().putString(PREF_SYSTEM_TIME, systemTime).apply()
    }
    
    fun getSystemTime(): Date {
        val dateString = pref.getString(PREF_SYSTEM_TIME, null)
        val deviceDate = Date()
        try {
            dateString?.isNotNullBlank {
                val serverDate = Utils.getDate(it)
                return if (deviceDate.after(serverDate)) deviceDate else serverDate
            }
        } catch (pe: ParseException) {
            pe.printStackTrace()
            ToffeeAnalytics.logException(pe)
        }
        
        return deviceDate
    }
    
    fun setDBVersion(dbVersionList: List<DBVersionV2>) {
        for (dbVersion in dbVersionList) {
            val prefDbVersion = pref.getInt(dbVersion.apiName, 0)
            if (dbVersion.dbVersion > prefDbVersion) {
                pref.edit().putInt(dbVersion.apiName, dbVersion.dbVersion).apply()
            }
        }
    }
    
    fun getDBVersionByApiName(apiName: String): Int {
        return pref.getInt(apiName, 0)
    }
    
    fun updateDbVersionByApiName(apiName: String) {
        val dbVersion = getDBVersionByApiName(apiName) + 1
        pref.edit().putInt(apiName, dbVersion).apply()
    }
    
    fun clear() {
        pref.edit().clear().apply()
    }
    
    fun watchOnlyWifi(): Boolean {
        return pref.getBoolean(PREF_WATCH_ONLY_WIFI, false)
    }
    
    fun setWatchOnlyWifi(value: Boolean) {
        pref.edit().putBoolean(PREF_WATCH_ONLY_WIFI, value).apply()
    }
    
    var isBubbleEnabled: Boolean
        get() = pref.getBoolean(PREF_BUBBLE_ENABLED, true)
        set(value) = pref.edit().putBoolean(PREF_BUBBLE_ENABLED, value).apply()
    
    fun isNotificationEnabled(): Boolean {
        return pref.getBoolean(PREF_KEY_NOTIFICATION, true)
    }
    
    fun setNotificationEnabled(value: Boolean) {
        pref.edit { putBoolean(PREF_KEY_NOTIFICATION, value) }
    }
    
    fun setHeaderSessionToken(sessionToken: String?) {
        pref.edit().putString(PREF_SESSION_TOKEN_HEADER, sessionToken).apply()
    }
    
    fun getHeaderSessionToken(): String? {
        return pref.getString(PREF_SESSION_TOKEN_HEADER, "")
    }
    
    var shouldOverrideHlsHostUrl: Boolean
        get() = pref.getBoolean(PREF_SHOULD_OVERRIDE_HLS_URL, false)
        set(value) = pref.edit { putBoolean(PREF_SHOULD_OVERRIDE_HLS_URL, value) }
    
    var overrideHlsHostUrl: String
        get() = pref.getString(PREF_HLS_OVERRIDE_URL, "") ?: ""
        set(value) = pref.edit { putString(PREF_HLS_OVERRIDE_URL, value) }
    
    var shouldOverrideDrmHostUrl: Boolean
        get() = pref.getBoolean(PREF_SHOULD_OVERRIDE_DRM_URL, false)
        set(value) = pref.edit { putBoolean(PREF_SHOULD_OVERRIDE_DRM_URL, value) }
    
    var overrideDrmHostUrl: String
        get() = pref.getString(PREF_DRM_OVERRIDE_URL, "") ?: ""
        set(value) = pref.edit { putString(PREF_DRM_OVERRIDE_URL, value) }
    
    var shouldOverrideNcgHostUrl: Boolean
        get() = pref.getBoolean(PREF_SHOULD_OVERRIDE_NCG_URL, false)
        set(value) = pref.edit { putBoolean(PREF_SHOULD_OVERRIDE_NCG_URL, value) }
    
    var overrideNcgHostUrl: String
        get() = pref.getString(PREF_NCG_OVERRIDE_URL, "") ?: ""
        set(value) = pref.edit { putString(PREF_NCG_OVERRIDE_URL, value) }
    
    var shouldOverrideImageHostUrl: Boolean
        get() = pref.getBoolean(PREF_SHOULD_OVERRIDE_IMAGE_URL, false)
        set(value) = pref.edit { putBoolean(PREF_SHOULD_OVERRIDE_IMAGE_URL, value) }
    
    var overrideImageHostUrl: String
        get() = pref.getString(PREF_IMAGE_OVERRIDE_URL, "") ?: ""
        set(value) = pref.edit { putString(PREF_IMAGE_OVERRIDE_URL, value) }
    
    var shouldOverrideBaseUrl: Boolean
        get() = pref.getBoolean(PREF_SHOULD_OVERRIDE_BASE_URL, false)
        set(value) = pref.edit { putBoolean(PREF_SHOULD_OVERRIDE_BASE_URL, value) }
    
    var overrideBaseUrl: String
        get() = pref.getString(PREF_BASE_OVERRIDE_URL, "") ?: ""
        set(value) = pref.edit { putString(PREF_BASE_OVERRIDE_URL, value) }
    
    var isAllTvChannelMenuEnabled: Boolean
        get() = pref.getBoolean(PREF_ALL_TV_CHANNEL_MENU, false)
        set(value) = pref.edit { putBoolean(PREF_ALL_TV_CHANNEL_MENU, value) }
    
    fun setSessionTokenLifeSpanInMillis(tokenLifeSpanInMillis: Long) {
        pref.edit().putLong(PREF_DEVICE_TIME_IN_MILLISECONDS, System.currentTimeMillis()).apply()
        pref.edit()
            .putLong(PREF_TOKEN_LIFE_SPAN, tokenLifeSpanInMillis - 10 * 60 * 1000)
            .apply() //10 minute cut off for safety. We will request for new token 10 minutes early
    }
    
    fun getSessionTokenLifeSpanInMillis(): Long {
        return pref.getLong(PREF_TOKEN_LIFE_SPAN, 3600000)//default token span set to 1 hour
    }
    
    fun getSessionTokenSaveTimeInMillis(): Long {
        return pref.getLong(PREF_DEVICE_TIME_IN_MILLISECONDS, System.currentTimeMillis())
    }

//    var uploadId: String?
//        get() = pref.getString("toffee-upload-id", null)
//        set(value) = pref.edit { putString("toffee-upload-id", value) }
    
    var viewCountDbUrl: String
        get() = pref.getString(PREF_VIEW_COUNT_DB_URL, "") ?: ""
        set(viewCountDbUrl) {
            val storedUrl = pref.getString(PREF_VIEW_COUNT_DB_URL, "") ?: ""//get stored url
            pref.edit().putString(PREF_VIEW_COUNT_DB_URL, viewCountDbUrl).apply()//save new url
            if (storedUrl.isEmpty() || !viewCountDbUrl.equals(storedUrl, true)) {
                viewCountDbUrlLiveData.postValue(viewCountDbUrl)//post if there is mismatch of url
            }
        }
    
    var reactionStatusDbUrl: String
        get() = pref.getString(PREF_REACTION_STATUS_DB_URL, "") ?: ""
        set(reactionStatusDbUrl) {
            val storedUrl = pref.getString(PREF_REACTION_STATUS_DB_URL, "") ?: ""//get stored url
            pref.edit().putString(PREF_REACTION_STATUS_DB_URL, reactionStatusDbUrl)
                .apply()//save new url
            if (storedUrl.isEmpty() || !reactionStatusDbUrl.equals(storedUrl, true)) {
                reactionStatusDbUrlLiveData.postValue(reactionStatusDbUrl)//post if there is mismatch of url
            }
        }
    
    var subscriberStatusDbUrl: String
        get() = pref.getString(PREF_SUBSCRIBER_STATUS_DB_URL, "") ?: ""
        set(subscriberStatusDbUrl) {
            val storedUrl = pref.getString(PREF_SUBSCRIBER_STATUS_DB_URL, "") ?: ""//get stored url
            pref.edit().putString(PREF_SUBSCRIBER_STATUS_DB_URL, subscriberStatusDbUrl)
                .apply()//save new url
            if (storedUrl.isEmpty() || !subscriberStatusDbUrl.equals(storedUrl, true)) {
                subscriberStatusDbUrlLiveData.postValue(subscriberStatusDbUrl)//post if there is mismatch of url
            }
        }
    
    var shareCountDbUrl: String
        get() = pref.getString(PREF_SHARE_COUNT_DB_URL, "") ?: ""
        set(shareCountDbUrl) {
            val storedUrl = pref.getString(PREF_SHARE_COUNT_DB_URL, "") ?: ""//get stored url
            pref.edit().putString(PREF_SHARE_COUNT_DB_URL, shareCountDbUrl).apply()//save new url
            if (storedUrl.isEmpty() || !shareCountDbUrl.equals(storedUrl, true)) {
                shareCountDbUrlLiveData.postValue(shareCountDbUrl)//post if there is mismatch of url
            }
        }
    
    var uploadStatus: Int
        get() = pref.getInt(PREF_TOFFEE_UPLOAD_STATUS, -1)
        set(value) = pref.edit { putInt(PREF_TOFFEE_UPLOAD_STATUS, value) }
    
    var isEnableFloatingWindow: Boolean
        get() = pref.getBoolean(PREF_ENABLE_FLOATING_WINDOW, true)
        set(value) = pref.edit { putBoolean(PREF_ENABLE_FLOATING_WINDOW, value) }
    
    var isAutoplayForRecommendedVideos: Boolean
        get() = pref.getBoolean(PREF_AUTO_PLAY_RECOMMENDED, true)
        set(value) = pref.edit { putBoolean(PREF_AUTO_PLAY_RECOMMENDED, value) }
    
    var isPreviousDbDeleted: Boolean
        get() = pref.getBoolean(PREF_IS_PREVIOUS_DB_DELETED, false)
        set(value) = pref.edit { putBoolean(PREF_IS_PREVIOUS_DB_DELETED, value) }
    
    var hasReactionDb: Boolean
        get() = pref.getBoolean(PREF_HAS_REACTION_DB, false)
        set(value) = pref.edit { putBoolean(PREF_HAS_REACTION_DB, value) }
    
    var mqttIsActive: Boolean
        get() = pref.getBoolean(PREF_MQTT_IS_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_MQTT_IS_ACTIVE, value) }
    
    var isMqttRealtimeSyncActive: Boolean
        get() = pref.getBoolean(PREF_IS_MQTT_REALTIME_SYNC_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_IS_MQTT_REALTIME_SYNC_ACTIVE, value) }
    
    var mqttHost: String
        get() = pref.getString(PREF_MQTT_HOST, "") ?: ""  //ssl://im.toffeelive.com:1883
        set(value) = pref.edit { putString(PREF_MQTT_HOST, value) }
    
    var mqttClientId: String
        get() = pref.getString(PREF_MQTT_CLIENT_ID, "") ?: ""
        set(value) = pref.edit { putString(PREF_MQTT_CLIENT_ID, value) }
    
    var mqttUserName: String
        get() = pref.getString(PREF_MQTT_USER_NAME, "") ?: ""
        set(value) = pref.edit { putString(PREF_MQTT_USER_NAME, value) }
    
    var mqttPassword: String
        get() = pref.getString(PREF_MQTT_PASSWORD, "") ?: ""  //12345678
        set(value) = pref.edit { putString(PREF_MQTT_PASSWORD, value) }
    
    var isCastEnabled: Boolean
        get() = pref.getBoolean(PREF_IS_CAST_ENABLED, false)
        set(value) = pref.edit { putBoolean(PREF_IS_CAST_ENABLED, value) }
    
    var isCastUrlOverride: Boolean
        get() = pref.getBoolean(PREF_IS_CAST_URL_OVERRIDE, false)
        set(value) = pref.edit { putBoolean(PREF_IS_CAST_URL_OVERRIDE, value) }
    
    var castOverrideUrl: String
        get() = pref.getString(PREF_CAST_OVERRIDE_URL, "") ?: ""
        set(value) = pref.edit { putString(PREF_CAST_OVERRIDE_URL, value) }
    
    var castReceiverId: String
        get() = pref.getString(PREF_CAST_RECEIVER_ID, "") ?: ""
        set(value) = pref.edit { putString(PREF_CAST_RECEIVER_ID, value) }
    
    var internetPackUrl: String
        get() = pref.getString(PREF_INTERNET_PACK_URL, "") ?: ""
        set(value) = pref.edit { putString(PREF_INTERNET_PACK_URL, value) }
    
    var tusUploadServerUrl: String
        get() = pref.getString(PREF_TUS_UPLOAD_SERVER_URL, "") ?: ""
        set(value) = pref.edit { putString(PREF_TUS_UPLOAD_SERVER_URL, value) }
    
    var privacyPolicyUrl: String
        get() = pref.getString(PREF_PRIVACY_POLICY_URL, "") ?: ""
        set(value) = pref.edit { putString(PREF_PRIVACY_POLICY_URL, value) }
    
    var creatorsPolicyUrl: String
        get() = pref.getString(PREF_CREATORS_POLICY_URL, "") ?: ""
        set(value) = pref.edit { putString(PREF_CREATORS_POLICY_URL, value) }
    
    var termsAndConditionUrl: String
        get() = pref.getString(PREF_TERMS_AND_CONDITIONS_URL, "") ?: ""
        set(value) = pref.edit { putString(PREF_TERMS_AND_CONDITIONS_URL, value) }
    
    var facebookPageUrl: String
        get() = pref.getString(PREF_FACEBOOK_PAGE_URL, "") ?: ""
        set(value) = pref.edit { putString(PREF_FACEBOOK_PAGE_URL, value) }
    
    var instagramPageUrl: String
        get() = pref.getString(PREF_INSTAGRAM_PAGE_URL, "") ?: ""
        set(value) = pref.edit { putString(PREF_INSTAGRAM_PAGE_URL, value) }
    
    var youtubePageUrl: String
        get() = pref.getString(PREF_YOUTUBE_PAGE_URL, "") ?: ""
        set(value) = pref.edit { putString(PREF_YOUTUBE_PAGE_URL, value) }
    
    var geoCity: String
        get() = pref.getString(PREF_GEO_CITY, "") ?: ""
        set(value) = pref.edit { putString(PREF_GEO_CITY, value) }
    
    var geoRegion: String
        get() = pref.getString(PREF_GEO_REGION, "") ?: ""
        set(value) = pref.edit { putString(PREF_GEO_REGION, value) }
    
    var geoLocation: String
        get() = pref.getString(PREF_GEO_LOCATION, "") ?: ""
        set(value) = pref.edit { putString(PREF_GEO_LOCATION, value) }
    
    var userIp: String
        get() = pref.getString(PREF_USER_IP, "") ?: ""
        set(value) = pref.edit { putString(PREF_USER_IP, value) }
    
    var screenCaptureEnabledUsers: Set<String>
        get() = pref.getStringSet(PREF_SCREEN_CAPTURE_USERS, setOf()) ?: setOf()
        set(value) = pref.edit { putStringSet(PREF_SCREEN_CAPTURE_USERS, value) }
    
    private var forcedUpdateVersions: String?
        get() = pref.getString(PREF_FORCE_UPDATE_VERSIONS, null)
        set(value) = pref.edit { putString(PREF_FORCE_UPDATE_VERSIONS, value) }
    
    fun shouldForceUpdate(versionCode: Int): Boolean {
        forcedUpdateVersions?.let {
            if (versionCode.toString() in it.split(",")) return true
        }
        return false
    }
    
    var isPipEnabled: Boolean
        get() = pref.getBoolean(PREF_PIP_ENABLED, true)
        set(value) = pref.edit { putBoolean(PREF_PIP_ENABLED, value) }
    
    var isVastActive: Boolean
        get() = pref.getBoolean(PREF_TOFFEE_IS_VAST_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_TOFFEE_IS_VAST_ACTIVE, value) }
    
    var isNativeAdActive: Boolean
        get() = pref.getBoolean(PREF_TOFFEE_IS_NATIVE_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_TOFFEE_IS_NATIVE_ACTIVE, value) }
    
    var vastFrequency: Int
        get() = pref.getInt(PREF_TOFFEE_VAST_FREQUENCY, 1)
        set(value) = pref.edit { putInt(PREF_TOFFEE_VAST_FREQUENCY, value) }
    
    var bucketDirectory: String?
        get() = pref.getString(PREF_BUCKET_DIRECTORY, null)
        set(value) = pref.edit { putString(PREF_BUCKET_DIRECTORY, value) }
    
    var isFcmEventActive: Boolean
        get() = pref.getBoolean(PREF_TOFFEE_IS_FCM_EVENT_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_TOFFEE_IS_FCM_EVENT_ACTIVE, value) }
    
    var isFbEventActive: Boolean
        get() = pref.getBoolean(PREF_TOFFEE_IS_FB_EVENT_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_TOFFEE_IS_FB_EVENT_ACTIVE, value) }
    
    var isDrmActive: Boolean
        get() = pref.getBoolean(PREF_TOFFEE_IS_GLOBAL_DRM_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_TOFFEE_IS_GLOBAL_DRM_ACTIVE, value) }
    
    var isGlobalCidActive: Boolean
        get() = pref.getBoolean(PREF_IS_GLOBAL_CID_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_IS_GLOBAL_CID_ACTIVE, value) }
    
    var globalCidName: String?
        get() = pref.getString(PREF_GLOBAL_CID_NAME, null)
        set(value) = pref.edit { putString(PREF_GLOBAL_CID_NAME, value) }
    
    var drmCastReceiver: String?
        get() = pref.getString(PREF_TOFFEE_DEFAULT_DRM_CAST_RECEIVER, null)
        set(value) = pref.edit { putString(PREF_TOFFEE_DEFAULT_DRM_CAST_RECEIVER, value) }
    
    var drmWidevineLicenseUrl: String?
        get() = pref.getString(PREF_WIDEVINE_LICENSE_URL, null)
        set(value) = pref.edit { putString(PREF_WIDEVINE_LICENSE_URL, value) }
    
    var drmFpsLicenseUrl: String?
        get() = pref.getString(PREF_FPS_LICENSE_URL, null)
        set(value) = pref.edit { putString(PREF_FPS_LICENSE_URL, value) }
    
    var drmPlayreadyLicenseUrl: String?
        get() = pref.getString(PREF_PLAYREADY_LICENSE_URL, null)
        set(value) = pref.edit { putString(PREF_PLAYREADY_LICENSE_URL, value) }
    
    var heUpdateDate: String?
        get() = pref.getString(PREF_HE_UPDATE_DATE, null)
        set(value) = pref.edit { putString(PREF_HE_UPDATE_DATE, value) }
    
    var adIdUpdateDate: String?
        get() = pref.getString(PREF_AD_ID_UPDATE_DATE, null)
        set(value) = pref.edit { putString(PREF_AD_ID_UPDATE_DATE, value) }
    
    var adId: String?
        get() = pref.getString(PREF_AD_ID, null)
        set(value) = pref.edit { putString(PREF_AD_ID, value) }
    
    var drmTokenUrl: String?
        get() = pref.getString(PREF_DRM_TOKEN_URL, null)
        set(value) = pref.edit { putString(PREF_DRM_TOKEN_URL, value) }
    
    var betaVersionCodes: String?
        get() = pref.getString(PREF_BETA_VERSION_CODES, null)
        set(value) = pref.edit { putString(PREF_BETA_VERSION_CODES, value) }
    
    var isPaidUser: Boolean
        get() = pref.getBoolean(PREF_PAYMENT_STATUS, false)
        set(value) = pref.edit { putBoolean(PREF_PAYMENT_STATUS, value) }
    
    var isFireworkActive: Boolean
        get() = pref.getBoolean(PREF_IS_FIREWORK_ACTIVE_ANDROID, false)
        set(value) = pref.edit { putBoolean(PREF_IS_FIREWORK_ACTIVE_ANDROID, value) }
    
    var isStingrayActive: Boolean
        get() = pref.getBoolean(PREF_IS_STINGRAY_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_IS_STINGRAY_ACTIVE, value) }
    
    var isMedalliaActive: Boolean
        get() = pref.getBoolean(PREF_IS_MEDALLIA_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_IS_MEDALLIA_ACTIVE, value) }
    
    var isConvivaActive: Boolean
        get() = pref.getBoolean(PREF_IS_CONVIVA_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_IS_CONVIVA_ACTIVE, value) }
    
    var isPlayerMonitoringActive: Boolean
        get() = pref.getBoolean(PREF_IS_PLAYER_MONITORING_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_IS_PLAYER_MONITORING_ACTIVE, value) }
    
    var showBuyInternetForAndroid: Boolean
        get() = pref.getBoolean(PREF_SHOW_BUY_INTERNET_PACK, false)
        set(value) = pref.edit { putBoolean(PREF_SHOW_BUY_INTERNET_PACK, value) }
    
    var maxBitRateWifi: Int
        get() = pref.getInt(PREF_PLAYER_MAX_BIT_RATE_WIFI, -1)
        set(value) = pref.edit { putInt(PREF_PLAYER_MAX_BIT_RATE_WIFI, value) }
    
    var maxBitRateCellular: Int
        get() = pref.getInt(PREF_PLAYER_MAX_BIT_RATE_CELLULAR, -1)
        set(value) = pref.edit { putInt(PREF_PLAYER_MAX_BIT_RATE_CELLULAR, value) }
    
    var isRetryActive: Boolean
        get() = pref.getBoolean(PREF_PLAYER_IS_RETRY_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_PLAYER_IS_RETRY_ACTIVE, value) }
    
    var isFallbackActive: Boolean
        get() = pref.getBoolean(PREF_PLAYER_IS_FALLBACK_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_PLAYER_IS_FALLBACK_ACTIVE, value) }
    
    var retryCount: Int
        get() = pref.getInt(PREF_PLAYER_RETRY_COUNT, -1)
        set(value) = pref.edit { putInt(PREF_PLAYER_RETRY_COUNT, value) }
    
    var retryWaitDuration: Int
        get() = pref.getInt(PREF_PLAYER_RETRY_WAIT_DURATION, -1)
        set(value) = pref.edit { putInt(PREF_PLAYER_RETRY_WAIT_DURATION, value) }
    
    var videoMinDuration: Int
        get() = pref.getInt(PREF_VIDEO_MIN_DURATION, -1)
        set(value) = pref.edit { putInt(PREF_VIDEO_MIN_DURATION, value) }
    
    var videoMaxDuration: Int
        get() = pref.getInt(PREF_VIDEO_MAX_DURATION, -1)
        set(value) = pref.edit { putInt(PREF_VIDEO_MAX_DURATION, value) }
    
    var lastLoginDateTime: String
        get() = pref.getString(PREF_LAST_LOGIN_DATE_TIME, "") ?: ""
        set(value) = pref.edit { putString(PREF_LAST_LOGIN_DATE_TIME, value) }
    
    var isBubbleActive: Boolean
        get() = pref.getBoolean(PREF_IS_BUBBLE_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_IS_BUBBLE_ACTIVE, value) }
    
    var bubbleDialogShowCount: Int
        get() = pref.getInt(PREF_BUBBLE_DIALOG_SHOW_COUNT, 0)
        set(value) = pref.edit { putInt(PREF_BUBBLE_DIALOG_SHOW_COUNT, value) }
    
    var featuredPartnerTitle: String
        get() = pref.getString(PREF_FEATURED_PARTNER_TITLE, "Featured Partner") ?: "Featured Partner"
        set(value) = pref.edit { putString(PREF_FEATURED_PARTNER_TITLE, value) }
    
    var internalTimeOut: Int
        get() = pref.getInt(PREF_INTERNAL_TIME_OUT, 60)
        set(value) = pref.edit { putInt(PREF_INTERNAL_TIME_OUT, value) }
    
    var externalTimeOut: Int
        get() = pref.getInt(PREF_EXTERNAL_TIME_OUT, 60)
        set(value) = pref.edit { putInt(PREF_EXTERNAL_TIME_OUT, value) }
    
    var circuitBreakerFirestoreCollectionName: String?
        get() = pref.getString(PREF_FIRESTORE_DB_COLLECTION_NAME, null)
        set(value) = pref.edit { putString(PREF_FIRESTORE_DB_COLLECTION_NAME, value) }
    
    var isTopBarActive: Boolean
        get() = pref.getBoolean(PREF_IS_TOP_BAR_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_IS_TOP_BAR_ACTIVE, value) }
    
    var topBarImagePathLight: String?
        get() = pref.getString(PREF_TOP_BAR_IMAGE_PATH_LIGHT, null)
        set(value) = pref.edit { putString(PREF_TOP_BAR_IMAGE_PATH_LIGHT, value) }
    
    var topBarImagePathDark: String?
        get() = pref.getString(PREF_TOP_BAR_IMAGE_PATH_DARK, null)
        set(value) = pref.edit { putString(PREF_TOP_BAR_IMAGE_PATH_DARK, value) }
    
    var topBarStartDate: String?
        get() = pref.getString(PREF_TOP_BAR_START_DATE, null)
        set(value) = pref.edit { putString(PREF_TOP_BAR_START_DATE, value) }
    
    var topBarEndDate: String?
        get() = pref.getString(PREF_TOP_BAR_END_DATE, null)
        set(value) = pref.edit { putString(PREF_TOP_BAR_END_DATE, value) }
    
    var topBarType: String?
        get() = pref.getString(PREF_TOP_BAR_TYPE, null)
        set(value) = pref.edit { putString(PREF_TOP_BAR_TYPE, value) }
    
    var isCircuitBreakerActive: Boolean
        get() = pref.getBoolean(PREF_IS_CIRCUIT_BREAKER_ACTIVE, false)
        set(value) = pref.edit { putBoolean(PREF_IS_CIRCUIT_BREAKER_ACTIVE, value) }
    
    fun saveCustomerInfo(customerInfoLogin: CustomerInfoLogin) {
        customerInfoLogin.let {
            balance = it.balance
            isVerifiedUser = it.verified_status
            customerId = it.customerId
            password = it.password ?: ""
            if (!it.customerName.isNullOrBlank()) {
                customerName = it.customerName!!
            }
            sessionToken = (it.sessionToken ?: "")
            if (!it.profileImage.isNullOrBlank()) {
                userImageUrl = it.profileImage
            }
            setHeaderSessionToken(it.headerSessionToken)
            overrideHlsHostUrl = customerInfoLogin.hlsOverrideUrl ?: ""
            shouldOverrideHlsHostUrl = customerInfoLogin.hlsUrlOverride
            setSessionTokenLifeSpanInMillis(it.tokenLifeSpan.toLong() * 1000 * 3600)
            if (it.isBanglalinkNumber != null) {
                isBanglalinkNumber = it.isBanglalinkNumber
            }
            it.dbVersionList?.doIfNotNullOrEmpty {
                setDBVersion(it.toList())
            }
            latitude = it.lat ?: ""
            longitude = it.lon ?: ""
            isSubscriptionActive = it.isSubscriptionActive ?: "false"
            viewCountDbUrl = it.viewCountDbUrl ?: ""
            reactionStatusDbUrl = it.reactionStatusDbUrl ?: ""
            subscriberStatusDbUrl = it.subscriberStatusDbUrl ?: ""
            shareCountDbUrl = it.shareCountDbUrl ?: ""
            isAllTvChannelMenuEnabled = it.isAllTvChannelsMenuEnabled
            isFeaturePartnerActive = it.isFeaturePartnerActive == "true"
            mqttHost = it.mqttUrl?.let { EncryptionUtil.encryptRequest(it) } ?: ""
            mqttIsActive = it.mqttIsActive == 1
            isMqttRealtimeSyncActive = it.isMqttRealtimeSyncActive == 1
            
            isCastEnabled = it.isCastEnabled == 1
            isCastUrlOverride = it.isCastUrlOverride == 1
            castReceiverId = it.castReceiverId ?: ""
            castOverrideUrl = it.castOverrideUrl ?: ""
            
            internetPackUrl = it.internetPackUrl ?: ""
            tusUploadServerUrl = it.tusUploadServerUrl ?: ""
            privacyPolicyUrl = it.privacyPolicyUrl ?: ""
            creatorsPolicyUrl = it.creatorsPolicyUrl ?: ""
            termsAndConditionUrl = it.termsAndConditionsUrl ?: ""
            facebookPageUrl = it.facebookPageUrl
            instagramPageUrl = it.instagramPageUrl
            youtubePageUrl = it.youtubePageUrl
            
            geoCity = it.geoCity ?: ""
            geoRegion = it.geoRegion ?: geoCity
            geoLocation = it.geoLocation ?: ""
            userIp = it.userIp ?: ""
            
            forcedUpdateVersions = it.forceUpdateVersionCodes
            isVastActive = it.isVastActive == 1
            vastFrequency = it.vastFrequency
            bucketDirectory = it.gcpVodBucketDirectory
            isFcmEventActive = it.isFcmEventActive == 1
            isFbEventActive = it.isFbEventActive == 1
            isDrmActive = it.isGlobalDrmActive == 1
            drmCastReceiver = it.defaultDrmCastReceiver
            drmWidevineLicenseUrl = it.widevineLicenseUrl
            drmFpsLicenseUrl = it.fpsLicenseUrl
            drmPlayreadyLicenseUrl = it.playreadyLicenseUrl
            drmTokenUrl = it.drmTokenUrl
            isGlobalCidActive = it.isGlobalCidActive == 1
            globalCidName = it.globalCidName
            betaVersionCodes = it.androidBetaVersionCode
            isPaidUser = it.paymentStatus
            isFireworkActive = it.isFireworkActive
            isStingrayActive = it.isStingrayActive
            isMedalliaActive = it.isMedalliaActive
            isConvivaActive = it.isConvivaActive
            isPlayerMonitoringActive = it.isPlayerMonitoringActive
            showBuyInternetForAndroid = it.showBuyInternetForAndroid
            screenCaptureEnabledUsers = it.screenCaptureEnabledUsers ?: setOf()
            isNativeAdActive = it.isNativeAdActive
            maxBitRateWifi = it.maxBitRateWifi
            maxBitRateCellular = it.maxBitRateCellular
            isRetryActive = it.isRetryActive
            isFallbackActive = it.isFallbackActive
            retryCount = it.retryCount
            retryWaitDuration = it.retryWaitDuration
            videoMinDuration = it.videoMinDuration
            videoMaxDuration = it.videoMaxDuration
            isBubbleActive = it.bubbleConfig?.isBubbleActive ?: false
            bubbleConfigLiveData.value = it.bubbleConfig
            internalTimeOut = it.internalTimeOut?.takeIf { it >=5 } ?: 60
            externalTimeOut = it.externalTimeout?.takeIf { it >=5 } ?: 60
            circuitBreakerFirestoreCollectionName = it.fStoreTblContentBlacklist
            featuredPartnerTitle = it.featuredPartnerTitle ?: "Featured Partner"
            isCircuitBreakerActive = it.isCircuitBreakerActive
            activePremiumPackList.value = it.activePackList
            
            if (it.customerId == 0 || it.password.isNullOrBlank()) {
                ToffeeAnalytics.logException(NullPointerException("customerId: ${it.customerId}, password: ${it.password}, msisdn: $phoneNumber, deviceId: ${CommonPreference.getInstance().deviceId}, isVerified: $isVerifiedUser, hasSessionToken: ${sessionToken.isNotBlank()}"))
            }
        }
    }
    
    companion object {
        private const val PREF_PHONE_NUMBER = "p_number"
        private const val PREF_HE_PHONE_NUMBER = "he_p_number"
        private const val PREF_CUSTOMER_NAME = "customer_name"
        private const val PREF_CUSTOMER_EMAIL = "customer_email"
        private const val PREF_CUSTOMER_ADDRESS = "customer_address"
        private const val PREF_CUSTOMER_DOB = "customer_dob"
        private const val PREF_CUSTOMER_NID = "customer_nid"
        private const val PREF_CUSTOMER_ID = "customer_id"
        private const val PREF_PASSWORD = "passwd"
        private const val PREF_CHANNEL_ID = "channel_id"
        private const val PREF_SESSION_TOKEN = "session_token"
        private const val PREF_BANGLALINK_NUMBER = "banglalink_number"
        private const val PREF_HE_BANGLALINK_NUMBER = "he_banglalink_number"
        private const val PREF_VERIFICATION = "VER"
        private const val PREF_BALANCE = "balance"
        private const val PREF_LATITUDE = "latitude"
        private const val PREF_LONGITUDE = "Longitude"
        private const val PREF_FCM_TOKEN = "FCMToken"
        private const val PREF_IMAGE_URL = "image_url"
        private const val PREF_WIFI = "WIFI"
        private const val PREF_CELLULAR = "CELLULAR"
        private const val PREF_SUBSCRIPTION_ACTIVE = "subscription_active"
        private const val PREF_FEATURE_PARTNER_ACTIVE = "is_feature_partner_active_new"
        private const val PREF_FIREWORK_USER_ID = "firework_user_id"
        private const val PREF_SYSTEM_TIME = "systemTime"
        private const val PREF_WATCH_ONLY_WIFI = "WatchOnlyWifi"
        private const val PREF_BUBBLE_ENABLED = "bubbleEnabled"
        private const val PREF_KEY_NOTIFICATION = "pref_key_notification"
        private const val PREF_SESSION_TOKEN_HEADER = "sessionTokenHeader"
        private const val PREF_SHOULD_OVERRIDE_HLS_URL = "shouldOverrideHlsUrl"
        private const val PREF_HLS_OVERRIDE_URL = "hlsOverrideUrl"
        private const val PREF_SHOULD_OVERRIDE_DRM_URL = "shouldOverrideDrmUrl"
        private const val PREF_DRM_OVERRIDE_URL = "drmOverrideUrl"
        private const val PREF_SHOULD_OVERRIDE_NCG_URL = "shouldOverrideNcgUrl"
        private const val PREF_NCG_OVERRIDE_URL = "ncgOverrideUrl"
        private const val PREF_SHOULD_OVERRIDE_IMAGE_URL = "shouldOverrideImageUrl"
        private const val PREF_IMAGE_OVERRIDE_URL = "imageOverrideUrl"
        private const val PREF_SHOULD_OVERRIDE_BASE_URL = "shouldOverrideBaseUrl"
        private const val PREF_BASE_OVERRIDE_URL = "baseOverrideUrl"
        private const val PREF_ALL_TV_CHANNEL_MENU = "isAllTvChannelMenuEnabled"
        private const val PREF_DEVICE_TIME_IN_MILLISECONDS = "deviceTimeInMillis"
        private const val PREF_TOKEN_LIFE_SPAN = "tokenLifeSpan"
        private const val PREF_VIEW_COUNT_DB_URL = "viewCountDbUrl"
        private const val PREF_REACTION_DB_URL = "reactionDbUrl"
        private const val PREF_REACTION_STATUS_DB_URL = "reactionStatusDbUrl"
        private const val PREF_SUBSCRIBE_DB_URL = "subscribeDbUrl"
        private const val PREF_SUBSCRIBER_STATUS_DB_URL = "subscriberStatusDbUrl"
        private const val PREF_SHARE_COUNT_DB_URL = "shareCountDbUrl"
        private const val PREF_TOFFEE_UPLOAD_STATUS = "toffee-upload-status"
        private const val PREF_ENABLE_FLOATING_WINDOW = "enable-floating-window"
        private const val PREF_AUTO_PLAY_RECOMMENDED = "autoplay-for-recommended"
        private const val PREF_CHANNEL_LOGO = "channel_logo"
        private const val PREF_CHANNEL_NAME = "channel_name"
        private const val PREF_IS_PREVIOUS_DB_DELETED = "isPreviousDBDELETE"
        private const val PREF_IS_CHANNEL_DETAIL_CHECKED = "isChannelDetailChecked"
        private const val PREF_HAS_REACTION_DB = "pref_has_reaction_db"
        private const val PREF_MQTT_IS_ACTIVE = "pref_mqtt_is_active"
        private const val PREF_IS_MQTT_REALTIME_SYNC_ACTIVE = "pref_is_mqtt_realtime_sync_active"
        private const val PREF_MQTT_HOST = "pref_mqtt_host"
        private const val PREF_MQTT_CLIENT_ID = "pref_mqtt_client_id"
        private const val PREF_MQTT_USER_NAME = "pref_mqtt_user_name"
        private const val PREF_MQTT_PASSWORD = "pref_mqtt_password"
        private const val PREF_IS_CAST_ENABLED = "pref_is_cast_enabled"
        private const val PREF_IS_CAST_URL_OVERRIDE = "pref_is_cast_url_override"
        private const val PREF_CAST_RECEIVER_ID = "pref_cast_receiver_id"
        private const val PREF_CAST_OVERRIDE_URL = "pref_cast_override_url"
        private const val PREF_INTERNET_PACK_URL = "internet_pack_url"
        private const val PREF_TUS_UPLOAD_SERVER_URL = "tus_upload_server_url"
        private const val PREF_PRIVACY_POLICY_URL = "privacy_policy_url"
        private const val PREF_CREATORS_POLICY_URL = "creators_policy_url"
        private const val PREF_TERMS_AND_CONDITIONS_URL = "terms_and_conditions_url"
        private const val PREF_FACEBOOK_PAGE_URL = "facebook_page_url"
        private const val PREF_INSTAGRAM_PAGE_URL = "instagram_page_url"
        private const val PREF_YOUTUBE_PAGE_URL = "youtube_page_url"
        private const val PREF_GEO_CITY = "geo_city"
        private const val PREF_GEO_REGION = "geo_region"
        private const val PREF_GEO_LOCATION = "geo_location"
        private const val PREF_USER_IP = "user_ip"
        private const val PREF_SCREEN_CAPTURE_USERS = "screenCaptureEnabledUsers"
        private const val PREF_NAME_IP_TV = "IP_TV"
        private const val PREF_FORCE_UPDATE_VERSIONS = "pref_force_update_versions"
        private const val PREF_TOFFEE_IS_VAST_ACTIVE = "pref_is_vast_active"
        private const val PREF_TOFFEE_IS_NATIVE_ACTIVE = "pref_is_native_active"
        private const val PREF_TOFFEE_IS_FCM_EVENT_ACTIVE = "pref_is_fcm_event_active"
        private const val PREF_TOFFEE_IS_FB_EVENT_ACTIVE = "pref_is_fb_event_active"
        private const val PREF_TOFFEE_IS_GLOBAL_DRM_ACTIVE = "pref_is_global_drm_active"
        private const val PREF_TOFFEE_DEFAULT_DRM_CAST_RECEIVER = "pref_default_drm_cast_receiver"
        private const val PREF_TOFFEE_VAST_FREQUENCY = "pref_vast_frequency"
        private const val PREF_BUCKET_DIRECTORY = "pref_bucket_directory"
        private const val PREF_WIDEVINE_LICENSE_URL = "pref_widevine_license_url"
        private const val PREF_FPS_LICENSE_URL = "pref_fps_license_url"
        private const val PREF_PLAYREADY_LICENSE_URL = "pref_playready_license_url"
        private const val PREF_HE_UPDATE_DATE = "pref_he_update_date"
        private const val PREF_AD_ID_UPDATE_DATE = "pref_ad_id_update_date"
        private const val PREF_AD_ID = "pref_ad_id"
        private const val PREF_PIP_ENABLED = "pref_pip_enabled"
        private const val PREF_DRM_TOKEN_URL = "pref_drm_token_url"
        private const val PREF_IS_GLOBAL_CID_ACTIVE = "pref_is_global_cid_active"
        private const val PREF_GLOBAL_CID_NAME = "pref_global_cid_name"
        private const val PREF_BETA_VERSION_CODES = "pref_beta_version_codes"
        private const val PREF_PAYMENT_STATUS = "pref_payment_status"
        private const val PREF_IS_FIREWORK_ACTIVE_ANDROID = "pref_firework_active_status_android"
        private const val PREF_IS_STINGRAY_ACTIVE = "pref_stingray_active"
        private const val PREF_IS_MEDALLIA_ACTIVE = "pref_medallia_active"
        private const val PREF_IS_CONVIVA_ACTIVE = "pref_conviva_active"
        private const val PREF_IS_PLAYER_MONITORING_ACTIVE = "pref_is_player_monitoring_active"
        private const val PREF_SHOW_BUY_INTERNET_PACK = "pref_show_buy_internet_pack"
        private const val PREF_PLAYER_MAX_BIT_RATE_WIFI = "pref_player_max_bit_rate_wifi"
        private const val PREF_PLAYER_MAX_BIT_RATE_CELLULAR = "pref_player_max_bit_rate_cellular"
        private const val PREF_PLAYER_IS_RETRY_ACTIVE = "pref_player_is_retry_active"
        private const val PREF_PLAYER_IS_FALLBACK_ACTIVE = "pref_player_is_fallback_active"
        private const val PREF_PLAYER_RETRY_COUNT = "pref_player_retry_Count"
        private const val PREF_PLAYER_RETRY_WAIT_DURATION = "pref_player_retry_wait_duration"
        private const val PREF_VIDEO_MIN_DURATION = "pref_video_min_duration"
        private const val PREF_VIDEO_MAX_DURATION = "pref_video_max_duration"
        private const val PREF_LAST_LOGIN_DATE_TIME = "pref_last_login_date_time"
        private const val PREF_IS_BUBBLE_ACTIVE = "pref_is_bubble_active"
        private const val PREF_BUBBLE_DIALOG_SHOW_COUNT = "pref_bubble_dialog_permission_show_count"
        private const val PREF_FEATURED_PARTNER_TITLE = "pref_featured_partner_title"
        private const val PREF_INTERNAL_TIME_OUT = "pref_internal_time_out"
        private const val PREF_EXTERNAL_TIME_OUT = "pref_external_time_out"
        private const val PREF_FIRESTORE_DB_COLLECTION_NAME = "pref_firestore_db_collection_name"
        private const val PREF_IS_TOP_BAR_ACTIVE = "pref_top_bar_is_active"
        private const val PREF_TOP_BAR_IMAGE_PATH_LIGHT = "pref_top_bar_image_path_light"
        private const val PREF_TOP_BAR_IMAGE_PATH_DARK = "pref_top_bar_image_path_dark"
        private const val PREF_TOP_BAR_START_DATE = "pref_top_bar_start_date"
        private const val PREF_TOP_BAR_END_DATE = "pref_top_bar_end_date"
        private const val PREF_TOP_BAR_TYPE = "pref_top_bar_type"
        private const val PREF_IS_CIRCUIT_BREAKER_ACTIVE = "pref_is_circuit_breaker_active"
        private var instance: SessionPreference? = null
        
        fun init(mContext: Context) {
            if (instance == null) {
                instance = SessionPreference(
                    mContext.getSharedPreferences(
                        PREF_NAME_IP_TV,
                        Context.MODE_PRIVATE
                    ), mContext
                )
            }
        }
        
        fun getInstance(): SessionPreference {
            if (instance == null) {
                throw InstantiationException("Instance is null...call init() first")
            }
            return instance as SessionPreference
        }
    }
}
