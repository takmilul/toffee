package com.banglalink.toffee.analytics

import android.content.Context
import android.os.Bundle
import com.banglalink.toffee.exception.Error
import com.banglalink.toffee.model.ChannelInfo
import com.google.firebase.analytics.FirebaseAnalytics

object ToffeeAnalytics {

    lateinit var firebaseAnalytics: FirebaseAnalytics

    fun initFireBaseAnalytics(context: Context){
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    fun updateCustomerId(customerId:Int){
        firebaseAnalytics.setUserId(customerId.toString())
    }

    fun logApiError(apiName:String, error:Error){
        val params = Bundle()
        params.putString("api_name",apiName)
        params.putString("error_msg", error.msg)
        firebaseAnalytics.logEvent("api_error",params)
    }

    fun apiLoginFailed(errorMsg:String){
        val params = Bundle()
        params.putString("error_msg", errorMsg)
        firebaseAnalytics.logEvent("api_login_failed",null)
    }

    fun playerError(channelInfo: ChannelInfo, msg:String){
        val params = Bundle()
        params.putString("channel_name",channelInfo.program_name)
        params.putString("error_msg", msg)
        firebaseAnalytics.logEvent("player_error",null)
    }
}