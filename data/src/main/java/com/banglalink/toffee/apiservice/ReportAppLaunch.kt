package com.banglalink.toffee.apiservice

import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.APP_LAUNCH_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.Gson

class ReportAppLaunch {

    private val gson = Gson()

    fun execute(){
        try {
            PubSubMessageUtil.sendMessage(gson.toJson(PubSubBaseRequest()), APP_LAUNCH_TOPIC)
        }catch (e:Exception){
            ToffeeAnalytics.logBreadCrumb("Exception in ReportAppLaunch")
            ToffeeAnalytics.logException(e)
        }
    }
}