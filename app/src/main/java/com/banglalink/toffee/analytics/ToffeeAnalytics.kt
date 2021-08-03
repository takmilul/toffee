package com.banglalink.toffee.analytics

import android.content.Context
import android.os.Bundle
import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.notification.API_ERROR_TRACK_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

object ToffeeAnalytics {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var appEventsLogger: AppEventsLogger
    private val gson = Gson()

    fun initFireBaseAnalytics(context: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }
    fun initAppEventsLogger(context: Context) {
        appEventsLogger = AppEventsLogger.newLogger(context)
    }

    fun updateCustomerId(customerId: Int) {
        firebaseAnalytics.setUserId(customerId.toString())
        FirebaseCrashlytics.getInstance().setUserId(customerId.toString())
    }

    fun logApiError(apiName: String?, errorMsg: String?,phoneNumber:String = SessionPreference.getInstance().phoneNumber) {
        if (apiName.isNullOrBlank() || errorMsg.isNullOrBlank())
            return

        val logMsg = gson.toJson(ApiFailData(apiName,errorMsg))
        PubSubMessageUtil.sendMessage(logMsg, API_ERROR_TRACK_TOPIC)

    }

//    fun apiLoginFailed(errorMsg: String) {
//        logApiError("apiLogin",errorMsg)
//    }

    fun playerError(channelInfo: ChannelInfo, msg: String) {
        val params = Bundle()
        params.putString("channel_name", channelInfo.program_name)
        params.putString("error_msg", msg)
        firebaseAnalytics.logEvent("player_error", params)
    }

    fun logForcePlay() {
        val params = Bundle()
        params.putString("msg", "force play occurred")
        firebaseAnalytics.logEvent("player_event", params)
    }

    fun logSubscription(mPackage: Package) {
        val params = Bundle()
        params.putString("amount", mPackage.price.toString())
        params.putString("package_name", mPackage.packageName)
        params.putString("package_id", mPackage.packageId.toString())
        firebaseAnalytics.logEvent("subscription", params)
    }

    fun logException(e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }

    fun logBreadCrumb(msg: String) {
        FirebaseCrashlytics.getInstance().log(msg)
    }

    fun logEvent(event: String,bundle: Bundle?) {
//        firebaseAnalytics.logEvent(event, params)

        appEventsLogger.logEvent(event)
    }

    class ApiFailData(
        @SerializedName("apiName")
        val apiName: String,
        @SerializedName("errorMsg")
        val apiError: String
    ) : PubSubBaseRequest()
}