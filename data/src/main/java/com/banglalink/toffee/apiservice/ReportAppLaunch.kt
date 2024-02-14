package com.banglalink.toffee.apiservice

import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.di.NetworkModuleLib
import com.banglalink.toffee.notification.APP_LAUNCH_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import kotlinx.serialization.encodeToString

class ReportAppLaunch {
    
    fun execute(){
        try {
            PubSubMessageUtil.send(PubSubBaseRequest(), APP_LAUNCH_TOPIC)
        }catch (e:Exception){
            ToffeeAnalytics.logBreadCrumb("Exception in ReportAppLaunch")
            ToffeeAnalytics.logException(e)
        }
    }
}