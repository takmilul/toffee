package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.mqttservice.ToffeeMqttService
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.SUBSCRIPTION_TOPIC
import com.banglalink.toffee.util.currentDateTime
import com.google.gson.Gson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class SendSubscribeEvent @Inject constructor(private val mqttService: ToffeeMqttService) {
    
    fun sendToPubSub(subscriptionInfo: SubscriptionInfo, status: Int){
        val subscriptionCountData = SubscriptionCountData(
            channelId = subscriptionInfo.channelId,
            subscriberId = subscriptionInfo.customerId,
            status = status,
            date_time=subscriptionInfo.getDate()
        )
        PubSubMessageUtil.send(subscriptionCountData, SUBSCRIPTION_TOPIC)
        mqttService.send(subscriptionCountData, SUBSCRIPTION_TOPIC)
    }
}

@Serializable
data class SubscriptionCountData(
    @SerialName("channel_id")
    val channelId: Int = 0,
    @SerialName("subscriber_id")
    val subscriberId: Int = 0,
    @SerialName("status")
    val status: Int = 0,
    @SerialName("device_id")
    val deviceId: String = CommonPreference.getInstance().deviceId,
    @SerialName("date_time")
    val date_time: String? = null,
    @SerialName("reportingTime")
    val reportingTime: String = currentDateTime
)
