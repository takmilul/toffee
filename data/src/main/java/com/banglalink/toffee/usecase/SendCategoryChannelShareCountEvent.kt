package com.banglalink.toffee.usecase

import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.CATEGORY_CHANNEL_SHARE_COUNT_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.util.currentDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SendCategoryChannelShareCountEvent @Inject constructor(
    private val json: Json,
    private val preference: SessionPreference,
) {
    
    suspend fun execute(contentType: String, contentId: Int, sharedUrl: String, sendToPubSub: Boolean = true) {
        if (sendToPubSub) {
            val categoryChannelShareCount = CategoryChannelShareData(preference.customerId.toLong(), contentType, contentId, sharedUrl)
            PubSubMessageUtil.sendMessage(json.encodeToString(categoryChannelShareCount), CATEGORY_CHANNEL_SHARE_COUNT_TOPIC)
        } else {
//            shareLogApiService.execute(channelInfo.id.toInt(), channelInfo.video_share_url)
        }
    }
}

@Serializable
data class CategoryChannelShareData(
    @SerialName("user_id")
    val customerId: Long,
    @SerialName("content_type")
    val contentType: String,
    @SerialName("content_id")
    val contentId: Int,
    @SerialName("shared_url")
    val sharedUrl: String,
    @SerialName("device_type")
    val deviceType: Int = Constants.DEVICE_TYPE,
    @SerialName("device_id")
    val deviceId: String = CommonPreference.getInstance().deviceId,
    @SerialName("app_version")
    val appVersion: String = CommonPreference.getInstance().appVersionName,
    @SerialName("date_time")
    val shareDateTime: String = currentDateTime,
    @SerialName("reportingTime")
    val reportingTime: String = currentDateTime
)
