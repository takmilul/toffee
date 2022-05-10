package com.banglalink.toffee.analytics

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.core.os.bundleOf
import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.API_ERROR_TRACK_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

object ToffeeAnalytics {
    
    private val gson = Gson()
    private lateinit var facebookAnalytics: AppEventsLogger
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var firebaseCrashlytics = FirebaseCrashlytics.getInstance()
    
    fun initFireBaseAnalytics(context: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }
    
    fun initFacebookAnalytics(context: Context) {
        facebookAnalytics = AppEventsLogger.newLogger(context)
    }
    
    fun updateCustomerId(customerId: Int) {
        firebaseAnalytics.setUserId(customerId.toString())
        firebaseCrashlytics.setUserId(customerId.toString())
    }
    
    fun logApiError(apiName: String?, errorMsg: String?, phoneNumber: String = SessionPreference.getInstance().phoneNumber) {
        if (apiName.isNullOrBlank() || errorMsg.isNullOrBlank()) {
            return
        }
        val logMsg = gson.toJson(ApiFailData(apiName, errorMsg))
        PubSubMessageUtil.sendMessage(logMsg, API_ERROR_TRACK_TOPIC)
    }
    
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
    
    fun logForcePlay() {
        val params = Bundle()
        params.putString("msg", "force play occurred")
        firebaseAnalytics.logEvent("player_event", params)
    }
    
    fun logException(e: Exception) {
        firebaseCrashlytics.recordException(e)
    }
    
    fun logBreadCrumb(msg: String) {
        firebaseCrashlytics.log(msg)
    }
    
    fun logEvent(event: String, params: Bundle? = null, isOnlyFcmEvent: Boolean = false) {
        if (SessionPreference.getInstance().isFcmEventActive) {
            firebaseAnalytics.logEvent(event, params)
        }
        if (SessionPreference.getInstance().isFbEventActive && !isOnlyFcmEvent) {
            facebookAnalytics.logEvent(event, params)
        }
    }
    
    fun logUserProperty(propertyMap: Map<String, String>) {
        propertyMap.forEach {
            firebaseAnalytics.setUserProperty(it.key, it.value)
        }
    }
    
    class ApiFailData(
        @SerializedName("apiName")
        val apiName: String,
        @SerializedName("errorMsg")
        val apiError: String
    ) : PubSubBaseRequest()
} 