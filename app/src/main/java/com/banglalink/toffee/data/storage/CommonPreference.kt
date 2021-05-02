package com.banglalink.toffee.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.core.content.edit

const val COMMON_PREF_NAME = "LIFETIME_DATA"

class CommonPreference(private val pref: SharedPreferences, private val context: Context) {
    
    companion object {
        private const val APP_VERSION = "app_version"
        private const val PREF_FORCE_LOGGED_OUT = "is_force_logged_out"
        private var instance: CommonPreference? = null
        
        fun init(mContext: Context) {
            if (instance == null) {
                instance = CommonPreference(mContext.getSharedPreferences(COMMON_PREF_NAME, Context.MODE_PRIVATE), mContext)
            }
        }

        fun getInstance(): CommonPreference {
            if (instance == null) {
                throw InstantiationException("Instance is null...call init() first")
            }
            return instance as CommonPreference
        }
    }

    var versionCode: Int
        get() = pref.getInt(APP_VERSION, 0)
        set(value) = pref.edit { putInt(APP_VERSION, value) }
    
    val deviceId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
    var isAlreadyForceLoggedOut: Boolean
        get() = pref.getBoolean(PREF_FORCE_LOGGED_OUT,false)
        set(isVerified){
            pref.edit().putBoolean(PREF_FORCE_LOGGED_OUT,isVerified).apply()
        }
    
}