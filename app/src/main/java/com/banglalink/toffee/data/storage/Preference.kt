package com.banglalink.toffee.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.model.CustomerInfoSignIn
import com.banglalink.toffee.model.DBVersion
import com.banglalink.toffee.model.DBVersionV2
import com.banglalink.toffee.util.Utils
import java.text.ParseException
import java.util.*

class Preference(private val pref: SharedPreferences,
                                     private val context: Context) {

    val viewCountDbUrlLiveData = MutableLiveData<String>()
    val sessionTokenLiveData = MutableLiveData<String>()
    val profileImageUrlLiveData = MutableLiveData<String>()
    val customerNameLiveData = MutableLiveData<String>()

    var phoneNumber: String
        get() = pref.getString(PHONE_NUMBER, "") ?: ""
        set(phoneNumber) = pref.edit{ putString(PHONE_NUMBER, phoneNumber) }

    var customerName: String
        get() = pref.getString(CUSTOMER_NAME, "") ?: ""
        set(customerName) {
            customerNameLiveData.postValue(customerName)
            pref.edit{ putString(CUSTOMER_NAME, customerName) }
        }

    var customerId: Int
        get() = pref.getInt(CUSTOMER_ID, 0)
        set(customerId) {
            pref.edit{ putInt(CUSTOMER_ID, customerId) }
        }

    var password: String
        get() = pref.getString(PASSWORD, "") ?: ""
        set(password) {
            pref.edit().putString(PASSWORD, password).apply()
        }

    var sessionToken: String
        get() = pref.getString("sessionToken", "") ?: ""
        set(sessionToken) {
            val storedToken = pref.getString("sessionToken", "") ?: ""//get stored token
            pref.edit().putString("sessionToken", sessionToken).apply()//save new session token
            if (storedToken.isNotEmpty() && !sessionToken.equals(storedToken, true)) {
                pref.edit().putLong("deviceTimeInMillis", System.currentTimeMillis()).apply()//Update session token change time
                sessionTokenLiveData.postValue(sessionToken)//post if there is mismatch of session token
            }
        }
    var isBanglalinkNumber:String
    get() = pref.getString("isBanglalinkNumber","false")?:"false"
    set(isBanglalinkNumber){
        pref.edit().putString("isBanglalinkNumber",isBanglalinkNumber).apply()
    }

    var balance: Int
        get() = pref.getInt("balance", 0)
        set(balance) {
            pref.edit().putInt("balance", balance).apply()
        }

    var latitude: String
        get() = pref.getString("latitude", "") ?: ""
        set(latitude) {
            pref.edit().putString("latitude", latitude).apply()
        }

    var longitude: String
        get() = pref.getString("Longitude", "") ?: ""
        set(Longitude) {
            pref.edit().putString("Longitude", Longitude).apply()
        }

    var wifiProfileStatus: Int
        get() = pref.getInt("WifiProfileStatus1", 6)
        set(value) {
            pref.edit().putInt("WifiProfileStatus1", value).apply()
        }

    var cellularProfileStatus: Int
        get() = pref.getInt("CellularProfileStatus4", 5)
        set(value) {
            pref.edit().putInt("CellularProfileStatus4", value).apply()
        }

    var channelDbVersion: Int
        get() = pref.getInt("channel_db_version", 0)
        set(version) {
            pref.edit().putInt("channel_db_version", version).apply()
        }

    var catchupDbVersion: Int
        get() = pref.getInt("catchup_db_version", 0)
        set(version) {
            pref.edit().putInt("catchup_db_version", version).apply()
        }

    var vodDbVersion: Int
        get() = pref.getInt("vod_db_version", 0)
        set(version) {
            pref.edit().putInt("vod_db_version", version).apply()
        }

    var packageDbVersion: Int
        get() = pref.getInt("package_db_version", 0)
        set(version) {
            pref.edit().putInt("package_db_version", version).apply()
        }

    var categoryDbVersion: Int
        get() = pref.getInt("category_db_version", 0)
        set(version) {
            pref.edit().putInt("category_db_version", version).apply()
        }

    var fcmToken: String
        get() = pref.getString("FCMToken", "") ?: ""
        set(token) {
            pref.edit().putString("FCMToken", token).apply()
        }

    var userImageUrl: String?
        get() = pref.getString("image_url", null)
        set(userPhoto) {
            pref.edit().putString("image_url", userPhoto).apply()
            if (!TextUtils.isEmpty(userPhoto))
                profileImageUrlLiveData.postValue(userPhoto)
        }

    var appThemeMode: Int
        get() = pref.getInt("app_theme", 0)
        set(themeMode){
            pref.edit().putInt("app_theme", themeMode).apply()
        }
    
    val netType: String
        get() = if (Utils.checkWifiOnAndConnected(context)) "WIFI" else "CELLULAR"

    var isSubscriptionActive: String
        get() = pref.getString("subscription_active", "") ?: ""
        set(phoneNumber) {
            pref.edit().putString("subscription_active", phoneNumber).apply()
        }
    
    var channelId: Int
        get() = pref.getInt(CHANNEL_ID, 0)
        set(channelId) = pref.edit().putInt(CHANNEL_ID, channelId).apply()
    
    fun setSystemTime(systemTime: String) {
        pref.edit().putString("systemTime", systemTime).apply()
    }

    fun getSystemTime(): Date {
        val dateString = pref.getString("systemTime", "")
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
        pref.edit().putInt("channel_db_version", dbVersion.chanelDbVersion).apply()
        pref.edit().putInt("vod_db_version", dbVersion.vodDbVersion).apply()
        pref.edit().putInt("notification_db_version", dbVersion.notificationDbVersion).apply()
        pref.edit().putInt("catchup_db_version", dbVersion.catchupDbVersion).apply()
        pref.edit().putInt("package_db_version", dbVersion.packageDbVersion).apply()
    }

    fun setDBVersion(dbVersionList: List<DBVersionV2>) {
        for(dbVersion in dbVersionList){
            pref.edit().putInt(dbVersion.apiName,dbVersion.dbVersion).apply()
        }
    }

    fun getDBVersionByApiName(apiName:String):Int{
        return pref.getInt(apiName,0)
    }

    fun clear() {
        pref.edit().clear().apply()
    }


    fun watchOnlyWifi(): Boolean {
        return pref.getBoolean("WatchOnlyWifi", false)
    }

    fun setWatchOnlyWifi(value: Boolean) {
        pref.edit().putBoolean("WatchOnlyWifi", value).apply()
    }

    fun isNotificationEnabled(): Boolean {
        return pref.getBoolean("pref_key_notification", true)
    }

    fun setNotificationEnabled(value: Boolean) {
        pref.edit { putBoolean("pref_key_notification", value) }
    }

    fun defaultDataQuality(): Boolean {
        return pref.getBoolean("DefaultDataQuality2", false)
    }

    fun setDefaultDataQuality(value: Boolean) {
        pref.edit().putBoolean("DefaultDataQuality2", value).apply()
    }

    fun setHeaderSessionToken(sessionToken: String?) {
        pref.edit().putString("sessionTokenHeader", sessionToken).apply()
    }

    fun getHeaderSessionToken(): String? {
        return pref.getString("sessionTokenHeader", "")
    }

    fun setHlsOverrideUrl(hlsOverrideUrl: String?) {
        pref.edit().putString("hlsOverrideUrl", hlsOverrideUrl).apply()
    }

    fun getHlsOverrideUrl(): String? {
        return pref.getString("hlsOverrideUrl", "")
    }

    fun setShouldOverrideHlsUrl(value: Boolean) {
        pref.edit().putBoolean("shouldOverride", value).apply()
    }

    fun shouldOverrideHlsUrl(): Boolean {
        return pref.getBoolean("shouldOverride", false)
    }

    fun setSessionTokenLifeSpanInMillis(tokenLifeSpanInMillis: Long) {
        pref.edit().putLong("deviceTimeInMillis", System.currentTimeMillis()).apply()
        pref.edit().putLong("tokenLifeSpan", tokenLifeSpanInMillis - 10 * 60 * 1000)
            .apply() //10 minute cut off for safety. We will request for new token 10 minutes early
    }

    fun getSessionTokenLifeSpanInMillis(): Long {
        return pref.getLong("tokenLifeSpan",  3600000)//default token span set to 1 hour
    }

    fun getSessionTokenSaveTimeInMillis(): Long {
        return pref.getLong("deviceTimeInMillis", System.currentTimeMillis());
    }

//    var uploadId: String?
//        get() = pref.getString("toffee-upload-id", null)
//        set(value) = pref.edit { putString("toffee-upload-id", value) }

    var viewCountDbUrl: String
        get() = pref.getString("viewCountDbUrl", "") ?: ""
        set(viewCountDbUrl) {
            val storedUrl = pref.getString("viewCountDbUrl", "") ?: ""//get stored url
            pref.edit().putString("viewCountDbUrl", viewCountDbUrl).apply()//save new url
            if (storedUrl.isEmpty() || !viewCountDbUrl.equals(storedUrl, true)) {
                viewCountDbUrlLiveData.postValue(viewCountDbUrl)//post if there is mismatch of url
            }
        }

    var uploadStatus: Int
        get() = pref.getInt("toffee-upload-status", -1)
        set(value) = pref.edit { putInt("toffee-upload-status", value) }

    var isEnableFloatingWindow: Boolean
        get() = pref.getBoolean("enable-floating-window", true)
        set(value) = pref.edit { putBoolean("enable-floating-window", value) }

    var isAutoplayForRecommendedVideos: Boolean
        get() = pref.getBoolean("autoplay-for-recommended", true)
        set(value) = pref.edit { putBoolean("autoplay-for-recommended", value) }

//    var uploadUri: String?
//        get() = pref.getString("toffee-upload-uri", null)
//        set(value) = pref.edit { putString("toffee-upload-uri", value) }

    fun saveCustomerInfo(customerInfoSignIn:CustomerInfoSignIn){
        balance = customerInfoSignIn.balance
        customerId = customerInfoSignIn.customerId
        password = customerInfoSignIn.password?:""
        customerName = customerInfoSignIn.customerName?:""
        sessionToken = (customerInfoSignIn.sessionToken?:"")

        setHeaderSessionToken(customerInfoSignIn.headerSessionToken)
        setHlsOverrideUrl(customerInfoSignIn.hlsOverrideUrl)
        setShouldOverrideHlsUrl(customerInfoSignIn.hlsUrlOverride)
        setSessionTokenLifeSpanInMillis(customerInfoSignIn.tokenLifeSpan.toLong() * 1000 * 3600)
        if(customerInfoSignIn.isBanglalinkNumber!=null){
            isBanglalinkNumber = customerInfoSignIn.isBanglalinkNumber
        }
        customerInfoSignIn.dbVersionList?.let {
            setDBVersion(it)
        }
        latitude = customerInfoSignIn.lat?:""
        longitude = customerInfoSignIn.long?:""
        isSubscriptionActive = customerInfoSignIn.isSubscriptionActive?:"true"
        viewCountDbUrl = (customerInfoSignIn.viewCountDbUrl?:"")
    }

    companion object {
        private const val PHONE_NUMBER = "p_number"
        private const val CUSTOMER_NAME = "customer_name"
        private const val CUSTOMER_ID = "customer_id"
        private const val PASSWORD = "passwd"
        private const val CHANNEL_ID = "channel_id"

        private var instance: Preference? = null

        fun init(mContext: Context) {
            if (instance == null) {
                instance = Preference(mContext.getSharedPreferences("IP_TV", Context.MODE_PRIVATE), mContext)
            }
        }

        fun getInstance(): Preference {
            if (instance == null) {
                throw InstantiationException("Instance is null...call init() first")
            }
            return instance as Preference
        }
    }
}
