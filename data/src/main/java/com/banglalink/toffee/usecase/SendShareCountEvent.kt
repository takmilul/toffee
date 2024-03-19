package com.banglalink.toffee.usecase

import com.banglalink.toffee.apiservice.SendShareLogApiService
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.mqttservice.ToffeeMqttService
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.SHARE_COUNT_TOPIC
import com.banglalink.toffee.util.currentDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class SendShareCountEvent @Inject constructor(
    private val preference: SessionPreference,
    private val mqttService: ToffeeMqttService,
    private val shareLogApiService: SendShareLogApiService,
) {
    
    suspend fun execute(channelInfo: ChannelInfo, sendToPubSub: Boolean = true) {
        if (sendToPubSub) {
            val contentId = channelInfo.getContentId()
            val shareCount = ShareData(preference.customerId, contentId.toLong())
            PubSubMessageUtil.sendMessage(shareCount, SHARE_COUNT_TOPIC)
            mqttService.send(shareCount, SHARE_COUNT_TOPIC)
        } else {
            shareLogApiService.execute(channelInfo.id.toInt(), channelInfo.video_share_url)
        }
    }
}

@Serializable
data class ShareData(
    @SerialName("subscriber_id")
    val customerId: Int = 0,
    @SerialName("content_id")
    val contentId: Long = 0,
    @SerialName("device_type")
    val deviceType: Int = 1,
    @SerialName("device_id")
    val deviceId: String = CommonPreference.getInstance().deviceId,
    @SerialName("date_time")
    val shareDateTime: String = currentDateTime,
    @SerialName("reportingTime")
    val reportingTime: String = currentDateTime
)
