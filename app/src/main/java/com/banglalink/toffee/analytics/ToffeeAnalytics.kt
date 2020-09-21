package com.banglalink.toffee.analytics

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Package
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.lang.Exception

object ToffeeAnalytics {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    fun initFireBaseAnalytics(context: Context){
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    fun updateCustomerId(customerId:Int){
        firebaseAnalytics.setUserId(customerId.toString())
        FirebaseCrashlytics.getInstance().setUserId(customerId.toString())
    }

    fun logApiError(apiName:String?, errorMsg: String?){
        if(TextUtils.isEmpty(apiName) || TextUtils.isEmpty(errorMsg))
            return

        val params = Bundle()
        params.putString("api_name",apiName)
        params.putString("error_msg",errorMsg)
        firebaseAnalytics.logEvent("api_error",params)
    }

    fun apiLoginFailed(errorMsg:String){
        val params = Bundle()
        params.putString("error_msg", errorMsg)
        firebaseAnalytics.logEvent("api_login_failed",params)
    }

    fun playerError(channelInfo: ChannelInfo, msg:String){
        val params = Bundle()
        params.putString("channel_name",channelInfo.program_name)
        params.putString("error_msg", msg)
        firebaseAnalytics.logEvent("player_error",params)
    }

    fun logForcePlay(){
        val params = Bundle()
        params.putString("msg", "force play occurred")
        firebaseAnalytics.logEvent("player_event",params)
    }

    fun logSubscription(mPackage: Package){
        val params = Bundle()
        params.putString("amount", mPackage.price.toString())
        params.putString("package_name", mPackage.packageName)
        params.putString("package_id", mPackage.packageId.toString())
        firebaseAnalytics.logEvent("subscription",params)
    }

    fun logException(e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }

    fun logBreadCrumb(msg:String){
        FirebaseCrashlytics.getInstance().log(msg)
    }
}