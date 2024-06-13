package com.banglalink.toffee.data.storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.*
import androidx.core.content.edit
import com.banglalink.toffee.util.Utils
import java.util.Locale

const val COMMON_PREF_NAME = "LIFETIME_DATA"

@SuppressLint("HardwareIds")
class CommonPreference(private val pref: SharedPreferences, private val context: Context) {
    
    companion object {
        private const val APP_VERSION = "app_version"
        private const val PREF_APP_THEME = "app_theme"
        private const val PREF_FORCE_LOGGED_OUT = "is_force_logged_out"
        private const val PREF_IS_USER_INTEREST_SUBMITTED = "_isUserInterestSubmitted"
        private const val PREF_DRM_AVAILABLE = "pref_is_drm_module_available"
        private var instance: CommonPreference? = null
        const val DRM_AVAILABLE = 0
        const val DRM_UNAVAILABLE = 1
        const val DRM_TIMEOUT = 2
        
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
    
    val appVersionName by lazy {
        Utils.getVersionInfo(context)?.first ?: "Unknown"
    }
    
    val appVersionCode by lazy {
        Utils.getVersionInfo(context)?.second ?: 0L
    }
    
    var versionCode: Int
        get() = pref.getInt(APP_VERSION, 0)
        set(value) = pref.edit { putInt(APP_VERSION, value) }
    
    val deviceId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
    }

    val deviceName: String by lazy {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        if (model.lowercase(Locale.ROOT).startsWith(manufacturer.lowercase(Locale.ROOT))) {
            model.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        } else {
            "${manufacturer.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }} $model"
        }
    }

    var appThemeMode: Int
        get() = pref.getInt(PREF_APP_THEME, Configuration.UI_MODE_NIGHT_YES)
        set(themeMode) {
            pref.edit().putInt(PREF_APP_THEME, themeMode).apply()
        }
    
    var isAlreadyForceLoggedOut: Boolean
        get() = pref.getBoolean(PREF_FORCE_LOGGED_OUT, false)
        set(isVerified) {
            pref.edit().putBoolean(PREF_FORCE_LOGGED_OUT, isVerified).apply()
        }
    
    var isDrmModuleAvailable: Int
        get() = pref.getInt(PREF_DRM_AVAILABLE, DRM_UNAVAILABLE)
        set(value) {
            pref.edit { putInt(PREF_DRM_AVAILABLE, value) }
        }
    
    val isTablet: Boolean
        get() = context.resources.configuration.smallestScreenWidthDp >= 600
    
    fun isUserInterestSubmitted(key: String): Boolean = pref.getBoolean(key + PREF_IS_USER_INTEREST_SUBMITTED, false)
    
    fun setUserInterestSubmitted(key: String) {
        pref.edit().putBoolean(key + PREF_IS_USER_INTEREST_SUBMITTED, true).apply()
    }
}