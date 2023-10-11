package com.banglalink.toffee.analytics

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import com.banglalink.toffee.analytics.ToffeeAnalytics.facebookAnalytics
import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.API_ERROR_TRACK_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Posts some events in [FirebaseAnalytics] and [facebookAnalytics]
 * Posts error logs in [FirebaseAnalytics]
 * Tracks custom logs in [FirebaseCrashlytics] log section.
 */
object ToffeeAnalytics {
    
    private val gson = Gson()
    private lateinit var facebookAnalytics: AppEventsLogger
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var firebaseCrashlytics = FirebaseCrashlytics.getInstance()
    
    /**
     * Initialize [FirebaseAnalytics]
     */
    fun initFirebaseAnalytics(context: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }
    
    /**
     * Initialize [facebookAnalytics]
     */
    fun initFacebookAnalytics(context: Context) {
        facebookAnalytics = AppEventsLogger.newLogger(context)
    }
    
    /**
     * Set UserId in [FirebaseAnalytics] and [FirebaseCrashlytics]
     */
    fun updateCustomerId(customerId: Int) {
        firebaseAnalytics.setUserId(customerId.toString())
        firebaseCrashlytics.setUserId(customerId.toString())
    }
    
    /**
     * Log API error in PubSub
     */
    fun logApiError(apiName: String?, errorMsg: String?, phoneNumber: String = SessionPreference.getInstance().phoneNumber) {
        if (apiName.isNullOrBlank() || errorMsg.isNullOrBlank()) {
            return
        }
        val logMsg = gson.toJson(ApiFailData(apiName, errorMsg))
        PubSubMessageUtil.sendMessage(logMsg, API_ERROR_TRACK_TOPIC)
    }
    
    /**
     * Log player error event in [FirebaseAnalytics]
     */
    fun playerError(programName: String, msg: String, isFallbackSucceeded: Boolean = false) {
        val mPref = SessionPreference.getInstance()
        val deviceId = CommonPreference.getInstance().deviceId
        val msisdn = mPref.phoneNumber.ifBlank { mPref.hePhoneNumber }
        val params = mapOf(
            "program_name" to programName,
            "error_message" to msg,
            "user_id" to mPref.customerId.toString(),
            "device_id" to deviceId,
            "msisdn" to msisdn,
            "brand" to Build.BRAND,
            "model" to Build.MODEL,
            "is_fallback_succeeded" to isFallbackSucceeded.toString()
        )
        val logValue = StringBuilder()
        params.map {
            logValue.appendLine(it)
        }
        val bundle = bundleOf(
            "error_value" to logValue.toString()
        )
        firebaseCrashlytics.log(logValue.toString())
        firebaseAnalytics.logEvent("player_error", bundle)
    }
    
    /**
     * Log Force Play event in [FirebaseAnalytics]
     */
    fun logForcePlay() {
        val params = Bundle()
        params.putString("msg", "force play occurred")
        firebaseAnalytics.logEvent("player_event", params)
    }
    
    /**
     * Log Exception in [FirebaseCrashlytics] as Non-Fatal issue
     */
    fun logException(e: Throwable) {
        firebaseCrashlytics.recordException(e)
    }
    
    /**
     * Log BreadCrumb message in [FirebaseCrashlytics]
     */
    fun logBreadCrumb(msg: String) {
        firebaseCrashlytics.log(msg)
    }
    
    /**
     * Log Events in [FirebaseAnalytics] and [facebookAnalytics]
     */
    fun logEvent(event: String, params: Bundle? = null, isOnlyFcmEvent: Boolean = false) {
        if (SessionPreference.getInstance().isFcmEventActive) {
            firebaseAnalytics.logEvent(event, params)
        }
        if (SessionPreference.getInstance().isFbEventActive && !isOnlyFcmEvent) {
            facebookAnalytics.logEvent(event, params)
        }
    }

    fun toffeeLogEvent(event: String, customParams: Bundle? = null, isOnlyFcmEvent: Boolean = false) {
        val commonParams = Bundle().apply {
            putString("app_version", CommonPreference.getInstance().appVersionName)
            putString("country", SessionPreference.getInstance().geoLocation)
            putString("device_model", CommonPreference.getInstance().deviceName )
//            putString("gender", null)
            putString("operating_system", "Android")
            putString("os_version", Build.VERSION.RELEASE)
            putString("platform", "Android")
            putString("region", SessionPreference.getInstance().geoRegion)
        }

        val combinedParams = customParams?.let { params ->
            val mergedParams = Bundle(commonParams).apply {
                putAll(params)
            }
            mergedParams
        } ?: commonParams

        // Replacing blank or null values with N/A
        combinedParams.keySet().forEach {
            if (combinedParams.getString(it).isNullOrBlank()) {
                combinedParams.putString(it, "N/A")
            }
        }

        if (SessionPreference.getInstance().isFcmEventActive) {
//            firebaseAnalytics.logEvent(event, combinedParams)
            Log.d("toffeeLogEvent", "Firebase event logged: $event, params: $combinedParams")
        }
        if (SessionPreference.getInstance().isFbEventActive && !isOnlyFcmEvent) {
//            facebookAnalytics.logEvent(event, combinedParams)
//            Log.d("toffeeLogEvent", "Facebook event logged: $event, params: $combinedParams")
        }
    }
    
    /**
     * Log some User Information in [FirebaseAnalytics]
     */
    fun logUserProperty(propertyMap: Map<String, String>) {
        propertyMap.forEach {
            firebaseAnalytics.setUserProperty(it.key, it.value)
        }
    }
    
    /**
     * API fail log in PUB/SUB
     */
    class ApiFailData(
        @SerializedName("apiName")
        val apiName: String,
        @SerializedName("errorMsg")
        val apiError: String
    ) : PubSubBaseRequest()
} 