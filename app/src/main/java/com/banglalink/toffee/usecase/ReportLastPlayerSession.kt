package com.banglalink.toffee.usecase

import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.network.request.PlayerSessionDetailsRequest
import com.banglalink.toffee.data.storage.PlayerPreference
import com.banglalink.toffee.notification.BANDWIDTH_TRACK_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportLastPlayerSession(private val playerPreference: PlayerPreference) {

    private val gson = Gson()

    suspend fun execute(){
        withContext(Dispatchers.IO){
            try {
                val sessionList = playerPreference.getPlayerSessionDetails()
                if(sessionList.isNotEmpty()){
                    val request = PlayerSessionDetailsRequest(sessionList)
                    PubSubMessageUtil.sendMessage(gson.toJson(request),BANDWIDTH_TRACK_TOPIC)
                }
            }catch (e:Exception){
                ToffeeAnalytics.logBreadCrumb("Exception in ReportLastPlayerSession")
            }

        }


    }
}