package com.banglalink.toffee.data.storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.text.TextUtils
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.model.CustomerInfoLogin
import com.banglalink.toffee.model.DBVersion
import com.banglalink.toffee.model.DBVersionV2
import com.banglalink.toffee.model.PlayerOverlayData
import com.banglalink.toffee.util.EncryptionUtil
import com.banglalink.toffee.util.SingleLiveEvent
import com.banglalink.toffee.util.Utils
import java.text.ParseException
import java.util.*

const val PREF_NAME_IP_TV= "IP_TV"

@SuppressLint("HardwareIds")
class SessionPreference(private val pref: SharedPreferences, private val context: Context) {

    val viewCountDbUrlLiveData = MutableLiveData<String>()
    val reactionDbUrlLiveData = MutableLiveData<String>()
    val reactionStatusDbUrlLiveData = MutableLiveData<String>()
    val subscribeDbUrlLiveData = MutableLiveData<String>()
    val subscriberStatusDbUrlLiveData = MutableLiveData<String>()
    val shareCountDbUrlLiveData = MutableLiveData<String>()
    val sessionTokenLiveData = MutableLiveData<String>()
    val profileImageUrlLiveData = MutableLiveData<String>()
    val customerNameLiveData = MutableLiveData<String>()
    val playerOverlayLiveData = SingleLiveEvent<PlayerOverlayData>()
    val forceLogoutUserLiveData = SingleLiveEvent<Boolean>()

    val deviceId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    var phoneNumber: String
        get() = pref.getString(PREF_PHONE_NUMBER, "") ?: ""
        set(phoneNumber) = pref.edit{ putString(PREF_PHONE_NUMBER, phoneNumber) }

    var customerName: String
        get() = pref.getString(PREF_CUSTOMER_NAME, "") ?: ""
        set(customerName) {
            customerNameLiveData.postValue(customerName)
            pref.edit{ putString(PREF_CUSTOMER_NAME, customerName) }
        }

    var customerId: Int
        get() = pref.getInt(PREF_CUSTOMER_ID, 0)
        set(customerId) {
            pref.edit{ putInt(PREF_CUSTOMER_ID, customerId) }
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
                pref.edit().putLong(PREF_DEVICE_TIME_IN_MILLISECONDS, System.currentTimeMillis()).apply()//Update session token change time
                sessionTokenLiveData.postValue(sessionToken)//post if there is mismatch of session token
            }
        }
    var isBanglalinkNumber:String
    get() = pref.getString(PREF_BANGLALINK_NUMBER,"false")?:"false"
    set(isBanglalinkNumber){
        pref.edit().putString(PREF_BANGLALINK_NUMBER,isBanglalinkNumber).apply()
    }

    var isVerifiedUser: Boolean
        get() = pref.getBoolean(PREF_VERFICATION,false)
        set(isVerified){
            pref.edit().putBoolean(PREF_VERFICATION,isVerified).apply()
        }
    
    var logout:String
        get() = pref.getString(PREF_LOGOUT,"0")?: ""
        set(logout){
            pref.edit().putString(PREF_LOGOUT,logout).apply()
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

    var wifiProfileStatus: Int
        get() = pref.getInt(PREF_WIFI_PROFILE_STATUS1, 6)
        set(value) {
            pref.edit().putInt(PREF_WIFI_PROFILE_STATUS1, value).apply()
        }

    var cellularProfileStatus: Int
        get() = pref.getInt(PREF_CELLULAR_PROFILE_STATUS4, 5)
        set(value) {
            pref.edit().putInt(PREF_CELLULAR_PROFILE_STATUS4, value).apply()
        }

    var channelDbVersion: Int
        get() = pref.getInt(PREF_CHANNEL_DB_VERSION, 0)
        set(version) {
            pref.edit().putInt(PREF_CHANNEL_DB_VERSION, version).apply()
        }

    var catchupDbVersion: Int
        get() = pref.getInt(PREF_CATCHUP_DB_VERSION, 0)
        set(version) {
            pref.edit().putInt(PREF_CATCHUP_DB_VERSION, version).apply()
        }

    var vodDbVersion: Int
        get() = pref.getInt(PREF_VOD_DB_VERSION, 0)
        set(version) {
            pref.edit().putInt(PREF_VOD_DB_VERSION, version).apply()
        }

    var packageDbVersion: Int
        get() = pref.getInt(PREF_PACKAGE_DB_VERSION, 0)
        set(version) {
            pref.edit().putInt(PREF_PACKAGE_DB_VERSION, version).apply()
        }

    var categoryDbVersion: Int
        get() = pref.getInt(PREF_CATEGORY_DB_VERSION, 0)
        set(version) {
            pref.edit().putInt(PREF_CATEGORY_DB_VERSION, version).apply()
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
            if (!TextUtils.isEmpty(userPhoto))
                profileImageUrlLiveData.postValue(userPhoto!!)
        }

    var appThemeMode: Int
        get() = pref.getInt(PREF_APP, 0)
        set(themeMode){
            pref.edit().putInt(PREF_APP, themeMode).apply()
        }

    val netType: String
        get() = if (Utils.checkWifiOnAndConnected(context)) PREF_WIFI else PREF_CELLULAR

    var isSubscriptionActive: String
        get() = pref.getString(PREF_SUBSCRIPTION_ACTIVE, "") ?: ""
        set(phoneNumber) {
            pref.edit().putString(PREF_SUBSCRIPTION_ACTIVE, phoneNumber).apply()
        }

    var isFireworkActive: String
        get() = pref.getString(PREF_FIREWORK_ACTIVE, "true") ?: "true"
        set(isActive) {
            pref.edit().putString(PREF_FIREWORK_ACTIVE, isActive).apply()
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
    
    fun hasChannelLogo(): Boolean{
        return channelLogo.isNotBlank()
    }
    
    fun hasChannelName(): Boolean{
        return channelName.isNotBlank()
    }
    
    fun setSystemTime(systemTime: String) {
        pref.edit().putString(PREF_SYSTEM_TIME, systemTime).apply()
    }

    fun getSystemTime(): Date {
        val dateString = pref.getString(PREF_SYSTEM_TIME, "")
        val deviceDate = Date()
        try{
            dateString?.let {
                val serverDate =  Utils.getDate(it)
                if(serverDate!=null){
                    return  if(deviceDate.after(serverDate)) deviceDate else serverDate//Date is after server date that means server date not updated. In that case use device time
                }
            }
        }catch (pe:ParseException){
            pe.printStackTrace()
            ToffeeAnalytics.logException(pe)
        }

        return deviceDate
    }

    fun setDBVersion(dbVersion: DBVersion) {
        pref.edit().putInt(PREF_CHANNEL_DB_VERSION, dbVersion.chanelDbVersion).apply()
        pref.edit().putInt(PREF_VOD_DB_VERSION, dbVersion.vodDbVersion).apply()
        pref.edit().putInt(PREF_NOTIFICATION_DB_VERSION, dbVersion.notificationDbVersion).apply()
        pref.edit().putInt(PREF_CATCHUP_DB_VERSION, dbVersion.catchupDbVersion).apply()
        pref.edit().putInt(PREF_PACKAGE_DB_VERSION, dbVersion.packageDbVersion).apply()
    }

    fun setDBVersion(dbVersionList: List<DBVersionV2>) {
        for(dbVersion in dbVersionList){
            pref.edit().putInt(dbVersion.apiName,dbVersion.dbVersion).apply()
        }
    }

    fun getDBVersionByApiName(apiName:String):Int{
        return pref.getInt(apiName,0)
    }

    fun updateDbVersionByApiName(apiName: String){
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

    fun isNotificationEnabled(): Boolean {
        return pref.getBoolean(PREF_KEY_NOTIFICATION, true)
    }

    fun setNotificationEnabled(value: Boolean) {
        pref.edit { putBoolean(PREF_KEY_NOTIFICATION, value) }
    }

    fun defaultDataQuality(): Boolean {
        return pref.getBoolean(PREF_DEFAULT_DATA_QUALITY_2, false)
    }

    fun setDefaultDataQuality(value: Boolean) {
        pref.edit().putBoolean(PREF_DEFAULT_DATA_QUALITY_2, value).apply()
    }

    fun setHeaderSessionToken(sessionToken: String?) {
        pref.edit().putString(PREF_SESSION_TOKEN_HEADER, sessionToken).apply()
    }

    fun getHeaderSessionToken(): String? {
        return pref.getString(PREF_SESSION_TOKEN_HEADER, "")
    }

    fun setHlsOverrideUrl(hlsOverrideUrl: String?) {
        pref.edit().putString(PREF_HLS_OVERRIDE_URL, hlsOverrideUrl).apply()
    }

    fun getHlsOverrideUrl(): String? {
        return pref.getString(PREF_HLS_OVERRIDE_URL, "")
    }

    fun setShouldOverrideHlsUrl(value: Boolean) {
        pref.edit().putBoolean(PREF_SHOULD_OVERRIDE, value).apply()
    }

    fun shouldOverrideHlsUrl(): Boolean {
        return pref.getBoolean(PREF_SHOULD_OVERRIDE, false)
    }

    fun setSessionTokenLifeSpanInMillis(tokenLifeSpanInMillis: Long) {
        pref.edit().putLong(PREF_DEVICE_TIME_IN_MILLISECONDS, System.currentTimeMillis()).apply()
        pref.edit().putLong(PREF_TOKEN_LIFE_SPAN, tokenLifeSpanInMillis - 10 * 60 * 1000)
            .apply() //10 minute cut off for safety. We will request for new token 10 minutes early
    }

    fun getSessionTokenLifeSpanInMillis(): Long {
        return pref.getLong(PREF_TOKEN_LIFE_SPAN,  3600000)//default token span set to 1 hour
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

    var reactionDbUrl: String
        get() = pref.getString(PREF_REACTION_DB_URL, "") ?: ""
        set(reactionDbUrl) {
            val storedUrl = pref.getString(PREF_REACTION_DB_URL, "") ?: ""//get stored url
            pref.edit().putString(PREF_REACTION_DB_URL, reactionDbUrl).apply()//save new url
            if (storedUrl.isEmpty() || !reactionDbUrl.equals(storedUrl, true)) {
                reactionDbUrlLiveData.postValue(reactionDbUrl)//post if there is mismatch of url
            }
        }

    var reactionStatusDbUrl: String
        get() = pref.getString(PREF_REACTION_STATUS_DB_URL, "") ?: ""
        set(reactionStatusDbUrl) {
            val storedUrl = pref.getString(PREF_REACTION_STATUS_DB_URL, "") ?: ""//get stored url
            pref.edit().putString(PREF_REACTION_STATUS_DB_URL, reactionStatusDbUrl).apply()//save new url
            if (storedUrl.isEmpty() || !reactionStatusDbUrl.equals(storedUrl, true)) {
                reactionStatusDbUrlLiveData.postValue(reactionStatusDbUrl)//post if there is mismatch of url
            }
        }

    var subscribeDbUrl: String
        get() = pref.getString(PREF_SUBSCRIBE_DB_URL, "") ?: ""
        set(subscribeDbUrl) {
            val storedUrl = pref.getString(PREF_SUBSCRIBE_DB_URL, "") ?: ""//get stored url
            pref.edit().putString(PREF_SUBSCRIBE_DB_URL, subscribeDbUrl).apply()//save new url
            if (storedUrl.isEmpty() || !subscribeDbUrl.equals(storedUrl, true)) {
                subscribeDbUrlLiveData.postValue(subscribeDbUrl)//post if there is mismatch of url
            }
        }

    var subscriberStatusDbUrl: String
        get() = pref.getString(PREF_SUBSCRIBER_STATUS_DB_URL, "") ?: ""
        set(subscriberStatusDbUrl) {
            val storedUrl = pref.getString(PREF_SUBSCRIBER_STATUS_DB_URL, "") ?: ""//get stored url
            pref.edit().putString(PREF_SUBSCRIBER_STATUS_DB_URL, subscriberStatusDbUrl).apply()//save new url
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
    
    var keepVideoAspectRatio: Boolean
        get() = pref.getBoolean(PREF_KEEP_ASPECT_RATIO, true)
        set(value) = pref.edit{ putBoolean(PREF_KEEP_ASPECT_RATIO, value) }

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
        get() = pref.getBoolean(PREF_MQTT_IS_ACTIVE, true)
        set(value) = pref.edit { putBoolean(PREF_MQTT_IS_ACTIVE, value) }
    
    var mqttHost: String
        get() = pref.getString(PREF_MQTT_HOST, "") ?: ""  //ssl://im.toffeelive.com:1883
        set(value) = pref.edit { putString(PREF_MQTT_HOST, value) }
    
    var mqttClientId: String
        get() = pref.getString(PREF_MQTT_CLIENT_ID, "") ?: ""  //evan-02@im.toffeelive.com
        set(value) = pref.edit { putString(PREF_MQTT_CLIENT_ID, value) }

    var mqttUserName: String
        get() = pref.getString(PREF_MQTT_USER_NAME, "") ?: ""  //evan-02@im.toffeelive.com
        set(value) = pref.edit { putString(PREF_MQTT_USER_NAME, value) }

    var mqttPassword: String
        get() = pref.getString(PREF_MQTT_PASSWORD, "") ?: ""  //12345678
        set(value) = pref.edit { putString(PREF_MQTT_PASSWORD, value) }

    var mqttTopic: String
        get() = pref.getString(PREF_MQTT_TOPIC, "test") ?: ""
        set(value) = pref.edit { putString(PREF_MQTT_TOPIC, value) }

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

//    var uploadUri: String?
//        get() = pref.getString("toffee-upload-uri", null)
//        set(value) = pref.edit { putString("toffee-upload-uri", value) }


    fun saveCustomerInfo(customerInfoLogin:CustomerInfoLogin){
        balance = customerInfoLogin.balance
        logout = "0"
        isVerifiedUser = customerInfoLogin.verified_status
        customerId = customerInfoLogin.customerId
        password = customerInfoLogin.password?:""
        customerName = customerInfoLogin.customerName?:""
        sessionToken = (customerInfoLogin.sessionToken?:"")

        setHeaderSessionToken(customerInfoLogin.headerSessionToken)
        setHlsOverrideUrl(customerInfoLogin.hlsOverrideUrl)
        setShouldOverrideHlsUrl(customerInfoLogin.hlsUrlOverride)
        setSessionTokenLifeSpanInMillis(customerInfoLogin.tokenLifeSpan.toLong() * 1000 * 3600)
        if(customerInfoLogin.isBanglalinkNumber!=null){
            isBanglalinkNumber = customerInfoLogin.isBanglalinkNumber
        }
        customerInfoLogin.dbVersionList?.let {
            setDBVersion(it)
        }
        latitude = customerInfoLogin.lat ?: ""
        longitude = customerInfoLogin.long ?: ""
        isSubscriptionActive = customerInfoLogin.isSubscriptionActive ?: "false"
        viewCountDbUrl = customerInfoLogin.viewCountDbUrl ?: ""
        reactionDbUrl = customerInfoLogin.reactionDbUrl ?: ""
        reactionStatusDbUrl = customerInfoLogin.reactionStatusDbUrl ?: ""
        subscribeDbUrl = customerInfoLogin.subscribeDbUrl ?: ""
        subscriberStatusDbUrl = customerInfoLogin.subscriberStatusDbUrl ?: ""
        shareCountDbUrl = customerInfoLogin.shareCountDbUrl ?: ""
        isFireworkActive = customerInfoLogin.isFireworkActive ?: "true"
        mqttHost = customerInfoLogin.mqttUrl?.let { EncryptionUtil.encryptRequest(it) } ?: ""
        mqttIsActive = customerInfoLogin.mqttIsActive == 1

        isCastEnabled = customerInfoLogin.isCastEnabled == 1
        isCastUrlOverride = customerInfoLogin.isCastUrlOverride == 1
        castReceiverId = customerInfoLogin.castReceiverId ?: ""
        castOverrideUrl = customerInfoLogin.castOverrideUrl ?: ""
    }

    companion object {
        private const val PREF_PHONE_NUMBER = "p_number"
        private const val PREF_CUSTOMER_NAME = "customer_name"
        private const val PREF_CUSTOMER_ID = "customer_id"
        private const val PREF_PASSWORD = "passwd"
        private const val PREF_CHANNEL_ID = "channel_id"
        private const val PREF_SESSION_TOKEN = "session_token"
        private const val PREF_BANGLALINK_NUMBER = "banglalink_number"
        private const val PREF_VERFICATION = "VER"
        private const val PREF_LOGOUT= "LOGIYT"
        private const val PREF_BALANCE = "balance"
        private const val PREF_LATITUDE= "latitude"
        private const val PREF_LONGITUDE= "Longitude"
        private const val PREF_WIFI_PROFILE_STATUS1= "WifiProfileStatus1"
        private const val PREF_CELLULAR_PROFILE_STATUS4= "CellularProfileStatus4"
        private const val PREF_CHANNEL_DB_VERSION= "channel_db_version"
        private const val PREF_CATCHUP_DB_VERSION= "catchup_db_version"
        private const val PREF_CATEGORY_DB_VERSION= "category_db_version"
        private const val PREF_VOD_DB_VERSION= "vod_db_version"
        private const val PREF_PACKAGE_DB_VERSION= "package_db_version"
        private const val PREF_NOTIFICATION_DB_VERSION= "notification_db_version"
        private const val PREF_FCM_TOKEN= "FCMToken"
        private const val PREF_IMAGE_URL= "image_url"
        private const val PREF_APP= "app_theme"
        private const val PREF_WIFI= "WIFI"
        private const val PREF_CELLULAR= "CELLULAR"
        private const val PREF_SUBSCRIPTION_ACTIVE= "subscription_active"
        private const val PREF_FIREWORK_ACTIVE= "firework_active"
        private const val PREF_SYSTEM_TIME= "systemTime"
        private const val PREF_WATCH_ONLY_WIFI= "WatchOnlyWifi"
        private const val PREF_KEY_NOTIFICATION= "pref_key_notification"
        private const val PREF_DEFAULT_DATA_QUALITY_2= "DefaultDataQuality2"
        private const val PREF_SESSION_TOKEN_HEADER= "sessionTokenHeader"
        private const val PREF_HLS_OVERRIDE_URL= "hlsOverrideUrl"
        private const val PREF_SHOULD_OVERRIDE= "shouldOverride"
        private const val PREF_DEVICE_TIME_IN_MILLISECONDS= "deviceTimeInMillis"
        private const val PREF_TOKEN_LIFE_SPAN= "tokenLifeSpan"
        private const val PREF_VIEW_COUNT_DB_URL= "viewCountDbUrl"
        private const val PREF_REACTION_DB_URL= "reactionDbUrl"
        private const val PREF_REACTION_STATUS_DB_URL= "reactionStatusDbUrl"
        private const val PREF_SUBSCRIBE_DB_URL= "subscribeDbUrl"
        private const val PREF_SUBSCRIBER_STATUS_DB_URL= "subscriberStatusDbUrl"
        private const val PREF_SHARE_COUNT_DB_URL= "shareCountDbUrl"
        private const val PREF_TOFFEE_UPLOAD_STATUS= "toffee-upload-status"
        private const val PREF_ENABLE_FLOATING_WINDOW= "enable-floating-window"
        private const val PREF_AUTO_PLAY_RECOMMENDED= "autoplay-for-recommended"
        private const val PREF_CHANNEL_LOGO = "channel_logo"
        private const val PREF_CHANNEL_NAME = "channel_name"
        private const val PREF_IS_PREVIOUS_DB_DELETED = "isPreviousDBDELETE"
        private const val PREF_IS_CHANNEL_DETAIL_CHECKED = "isChannelDetailChecked"
        private const val PREF_KEEP_ASPECT_RATIO = "pref_keep_aspect_ratio"
        private const val PREF_HAS_REACTION_DB = "pref_has_reaction_db"
        private const val PREF_MQTT_IS_ACTIVE = "pref_mqtt_is_active"
        private const val PREF_MQTT_HOST = "pref_mqtt_host"
        private const val PREF_MQTT_CLIENT_ID = "pref_mqtt_client_id"
        private const val PREF_MQTT_USER_NAME = "pref_mqtt_user_name"
        private const val PREF_MQTT_PASSWORD = "pref_mqtt_password"
        private const val PREF_MQTT_TOPIC = "pref_mqtt_topic"
        private const val PREF_IS_CAST_ENABLED = "pref_is_cast_enabled"
        private const val PREF_IS_CAST_URL_OVERRIDE = "pref_is_cast_url_override"
        private const val PREF_CAST_RECEIVER_ID = "pref_cast_receiver_id"
        private const val PREF_CAST_OVERRIDE_URL = "pref_cast_override_url"

        private const val PREF_NAME_IP_TV= "IP_TV"

        private var instance: SessionPreference? = null

        fun init(mContext: Context) {
            if (instance == null) {
                instance = SessionPreference(mContext.getSharedPreferences(PREF_NAME_IP_TV, Context.MODE_PRIVATE), mContext)
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
