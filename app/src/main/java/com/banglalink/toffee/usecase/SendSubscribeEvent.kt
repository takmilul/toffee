package com.banglalink.toffee.usecase

import android.util.Log
import com.banglalink.toffee.data.database.entities.SubscriptionCount
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.request.MyChannelSubscribeRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.SubscriptionCountRepository
import com.banglalink.toffee.data.repository.SubscriptionInfoRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.mqttservice.ToffeeMqttService
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.SUBSCRIPTION_TOPIC
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendSubscribeEvent @Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val mPref: SessionPreference,
    private val mqttService: ToffeeMqttService,
    private val subscriptionInfoRepository: SubscriptionInfoRepository, 
    private val subscriptionCountRepository: SubscriptionCountRepository,
) {
    
    var subscriptionCount: SubscriptionCount? =null
    
    suspend fun execute(subscriptionInfo: SubscriptionInfo, status: Int, sendToPubSub:Boolean = true){
        if(sendToPubSub){
            sendToPubSub(subscriptionInfo, status)
            updateSubscriptionInfoDb(subscriptionInfo, status)
            sendToToffeeServer(subscriptionInfo, status)
        }
    }

    private fun sendToPubSub(subscriptionInfo: SubscriptionInfo, status: Int){
        val subscriptionCountData = SubscriptionCountData(
            channelId = subscriptionInfo.channelId,
            subscriberId = subscriptionInfo.customerId,
            status = status,
            date_time=subscriptionInfo.getDate()
        )
        PubSubMessageUtil.sendMessage(Gson().toJson(subscriptionCountData), SUBSCRIPTION_TOPIC)
        mqttService.sendMessage(Gson().toJson(subscriptionCountData), SUBSCRIPTION_TOPIC)
        Log.e("Pubsub","data"+Gson().toJson(subscriptionCountData))
    }

    private suspend fun updateSubscriptionInfoDb(subscriptionInfo: SubscriptionInfo, status: Int){
        subscriptionCount=subscriptionCountRepository.getSubscriptionCount(subscriptionInfo.channelId)
        if (subscriptionCount!=null) {
            subscriptionCountRepository.updateSubscriptionCount(subscriptionInfo.channelId, status)
        }
        else {
            subscriptionCountRepository.insert(SubscriptionCount(subscriptionInfo.channelId, 1))
        }
        if (status == 1){
            subscriptionInfoRepository.insert(subscriptionInfo)
        }
        else{
            subscriptionInfoRepository.deleteSubscriptionInfo(subscriptionInfo.channelId, subscriptionInfo.customerId)
        }
    }

    private suspend fun sendToToffeeServer(subscriptionInfo: SubscriptionInfo, status: Int){
        tryIO2 {
            toffeeApi.subscribeOnMyChannel(
                MyChannelSubscribeRequest(
                    subscriptionInfo.channelId,
                    status.takeIf { it > 0 } ?: 0,
                    subscriptionInfo.channelId,
                    mPref.customerId,
                    mPref.password
                )
            )
        }
    }
}

data class SubscriptionCountData(
    @SerializedName("channel_id")
    val channelId: Int,
    @SerializedName("subscriber_id")
    val subscriberId: Int,
    @SerializedName("status")
    val status: Int,
    @SerializedName("date_time")
    val date_time: String
)
