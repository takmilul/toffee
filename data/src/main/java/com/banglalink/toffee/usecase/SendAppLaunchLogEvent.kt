package com.banglalink.toffee.usecase

import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.APP_LAUNCH_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil

class SendAppLaunchLogEvent {
    
    fun execute(){
        try {
            PubSubMessageUtil.sendMessage(PubSubBaseRequest(), APP_LAUNCH_TOPIC)
        }catch (e:Exception){
            ToffeeAnalytics.logBreadCrumb("Exception in ReportAppLaunch")
            ToffeeAnalytics.logException(e)
        }
    }
}