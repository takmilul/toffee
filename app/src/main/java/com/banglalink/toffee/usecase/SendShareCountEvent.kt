package com.banglalink.toffee.usecase

import com.banglalink.toffee.apiservice.SendShareLogApiService
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.toFormattedDate
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.mqttservice.ToffeeMqttService
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.SHARE_COUNT_TOPIC
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendShareCountEvent @Inject constructor(
    private val preference: SessionPreference,
    private val mqttService: ToffeeMqttService,
    private val shareLogApiService: SendShareLogApiService,
) {

    private val gson = Gson()

    suspend fun execute(channelInfo: ChannelInfo, sendToPubSub: Boolean = true) {
        if (sendToPubSub) {
            val shareCount = ShareData(preference.customerId, channelInfo.id.toLong())
            PubSubMessageUtil.sendMessage(gson.toJson(shareCount), SHARE_COUNT_TOPIC)
            mqttService.sendMessage(gson.toJson(shareCount), SHARE_COUNT_TOPIC)
        } else {
            shareLogApiService.execute(channelInfo.id.toInt(), channelInfo.video_share_url)
        }
    }
}

data class ShareData(
    @SerializedName("subscriber_id")
    val customerId: Int,
    @SerializedName("content_id")
    val contentId: Long,
    @SerializedName("device_type")
    val deviceType: Int = 1,
    @SerializedName("device_id")
    val deviceId: String = CommonPreference.getInstance().deviceId,
    @SerializedName("date_time")
    val shareDateTime: String = System.currentTimeMillis().toFormattedDate(),
)
