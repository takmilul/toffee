package com.banglalink.toffee.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.banglalink.toffee.model.DBVersion

class Preference private constructor(context: Context) {
    private val pref: SharedPreferences = context.getSharedPreferences("IP_TV", Context.MODE_PRIVATE)

    private val balanceLiveData = MutableLiveData<Int>()
    private val sessionTokenLiveData = MutableLiveData<Int>()
    private val channelDbVersionLiveData = MutableLiveData<Int>()
    private val catchupDbVersionLiveData = MutableLiveData<Int>()
    private val vodDbVersionLiveData = MutableLiveData<Int>()
    private val categoryDbVersionLiveData = MutableLiveData<Int>()

    var phoneNumber: String
        get() = pref.getString("p_number", "")?:""
        set(phoneNumber) {
            pref.edit().putString("p_number", phoneNumber).apply()
        }

    var customerName: String
        get() = pref.getString("customer_name", "")?:""
        set(customerName) {
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
            pref.edit().putString("sessionToken", sessionToken).apply()
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
        get() = pref.getInt("CellularProfileStatus4", 3)
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
        set(userPhoto) = pref.edit().putString("image_url", userPhoto).apply()

    val savedChannelInfoListResponse: String?
        get() {
            val response = pref.getString("channel_info_list", null)
            return if (TextUtils.isEmpty(response)) null else response
        }

    fun setSystemTime(systemTime: String) {
        pref.edit().putString("systemTime", systemTime).apply()
    }

    fun setDBVersion(dbVersion: DBVersion) {
        pref.edit().putInt("channelDbVersion", dbVersion.chanelDbVersion).apply()
        pref.edit().putInt("vodDbVersion", dbVersion.vodDbVersion).apply()
        pref.edit().putInt("notificationDbVersion", dbVersion.notificationDbVersion).apply()
        pref.edit().putInt("catchupDbVersion", dbVersion.catchupDbVersion).apply()
        pref.edit().putInt("packageDbVersion", dbVersion.packageDbVersion).apply()
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
        return pref.getBoolean("DefaultDataQuality", true)
    }

    fun setDefaultDataQuality(value: Boolean) {
        pref.edit().putBoolean("DefaultDataQuality", value).apply()
    }

    fun saveChannelInfoListResponse(response: String) {
        pref.edit().putString("channel_info_list", response).apply()
    }

    companion object {
        private var instance: Preference? = null

        fun init(context: Context) {
            if (instance == null) {
                instance = Preference(context)
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
