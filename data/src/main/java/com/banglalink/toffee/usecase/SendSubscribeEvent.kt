package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.mqttservice.ToffeeMqttService
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.SUBSCRIPTION_TOPIC
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendSubscribeEvent @Inject constructor(private val mqttService: ToffeeMqttService) {
    
    fun sendToPubSub(subscriptionInfo: SubscriptionInfo, status: Int){
        val subscriptionCountData = SubscriptionCountData(
            channelId = subscriptionInfo.channelId,
            subscriberId = subscriptionInfo.customerId,
            status = status,
            date_time=subscriptionInfo.getDate()
        )
        PubSubMessageUtil.sendMessage(Gson().toJson(subscriptionCountData), SUBSCRIPTION_TOPIC)
        mqttService.sendMessage(Gson().toJson(subscriptionCountData), SUBSCRIPTION_TOPIC)
    }
}

data class SubscriptionCountData(
    @SerializedName("channel_id")
    val channelId: Int,
    @SerializedName("subscriber_id")
    val subscriberId: Int,
    @SerializedName("status")
    val status: Int,
    @SerializedName("device_id")
    val deviceId: String = CommonPreference.getInstance().deviceId,
    @SerializedName("date_time")
    val date_time: String
)
