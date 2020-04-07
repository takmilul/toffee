package com.banglalink.toffee.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.banglalink.toffee.model.DBVersion
import com.google.android.exoplayer2.util.Util
import java.text.SimpleDateFormat
import java.util.*

class Preference private constructor(val context: Context) {
    private val pref: SharedPreferences = context.getSharedPreferences("IP_TV", Context.MODE_PRIVATE)

    val balanceLiveData = MutableLiveData<Int>()
    val sessionTokenLiveData = MutableLiveData<String>()
    val profileImageUrlLiveData = MutableLiveData<String>()
    val customerNameLiveData = MutableLiveData<String>()

    var phoneNumber: String
        get() = pref.getString("p_number", "")?:""
        set(phoneNumber) {
            pref.edit().putString("p_number", phoneNumber).apply()
        }

    var customerName: String
        get() = pref.getString("customer_name", "")?:""
        set(customerName) {
            customerNameLiveData.postValue(customerName)
            pref.edit().putString("customer_name", customerName).apply()
        }

    var customerId: Int
        get() = pref.getInt("customer_id", 0)
        set(customerId) {
            pref.edit().putInt("customer_id", customerId).apply()
        }

    var password: String
        get() = pref.getString("passwd", "")?:""
        set(password) {
            pref.edit().putString("passwd", password).apply()
        }

    var sessionToken: String
        get() = pref.getString("sessionToken", "")?:""
        set(sessionToken) {
            val storedToken = pref.getString("sessionToken", "")?:""//get stored token
            pref.edit().putString("sessionToken", sessionToken).apply()//save new session token
            if(!sessionToken.equals(storedToken,true)){
                sessionTokenLiveData.postValue(sessionToken)//post if there is mismatch of session token
            }
        }

    var balance: Int
        get() = pref.getInt("balance", 0)
        set(balance) {
            pref.edit().putInt("balance", balance).apply()
        }

    var latitude: String
        get() = pref.getString("latitude", "")?:""
        set(latitude) {
            pref.edit().putString("latitude", latitude).apply()
        }

    var longitude: String
        get() = pref.getString("Longitude", "")?:""
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

    //    "channel_db_version": 0,
    //            "vod_db_version": 0,
    //            "notification_db_version": 0,
    //            "catchup_db_version": 0,
    //            "package_db_version": 0,
    //            "category_db_version": 0
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
        get() = pref.getString("FCMToken", "")?:""
        set(token) {
            pref.edit().putString("FCMToken", token).apply()
        }

    var userImageUrl: String?
        get() = pref.getString("image_url", null)
        set(userPhoto) {
            pref.edit().putString("image_url", userPhoto).apply()
            if(!TextUtils.isEmpty(userPhoto))
                profileImageUrlLiveData.postValue(userPhoto)
        }

    fun setSystemTime(systemTime: String) {
        pref.edit().putString("systemTime", systemTime).apply()
    }

    fun getSystemTime(): Date {
        val dateString = pref.getString("systemTime","")
        if(!TextUtils.isEmpty(dateString)){
            val currentFormatter =
                SimpleDateFormat("yyyyMMddHHmmss") //20200425235959
            currentFormatter.timeZone = TimeZone.getTimeZone("UTC");
            return currentFormatter.parse(dateString)
        }
        return Date()
    }

    fun setDBVersion(dbVersion: DBVersion) {
        pref.edit().putInt("channel_db_version", dbVersion.chanelDbVersion).apply()
        pref.edit().putInt("vod_db_version", dbVersion.vodDbVersion).apply()
        pref.edit().putInt("notification_db_version", dbVersion.notificationDbVersion).apply()
        pref.edit().putInt("catchup_db_version", dbVersion.catchupDbVersion).apply()
        pref.edit().putInt("package_db_version", dbVersion.packageDbVersion).apply()
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
            .apply() //10minute threshold
    }

    fun getSessionTokenLifeSpanInMillis():Long{
        return pref.getLong("tokenLifeSpan",0);
    }

    fun getSessionTokenSaveTimeInMillis():Long{
        return pref.getLong("deviceTimeInMillis",System.currentTimeMillis());
    }

    fun getHeader():String{
        return Util.getUserAgent(context,"Toffee");
    }

    companion object {
        private var instance: Preference? = null

        fun init(context: Context) {
            if (instance == null) {
                instance = Preference(context.applicationContext)
            }
        }

        fun getInstance(): Preference {
            if(instance == null){
                throw InstantiationException("Instance is null...call init() first")
            }
            return instance as Preference
        }
    }
}
