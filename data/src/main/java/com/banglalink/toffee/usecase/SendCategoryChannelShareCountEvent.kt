package com.banglalink.toffee.usecase

import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.CATEGORY_CHANNEL_SHARE_COUNT_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.util.currentDateTime
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendCategoryChannelShareCountEvent @Inject constructor(
    private val preference: SessionPreference,
) {

    private val gson = GsonBuilder().disableHtmlEscaping().create()

    suspend fun execute(contentType: String, contentId: Int, sharedUrl: String, sendToPubSub: Boolean = true) {
        if (sendToPubSub) {
            val categoryChannelShareCount = CategoryChannelShareData(preference.customerId.toLong(), contentType, contentId, sharedUrl)
            PubSubMessageUtil.sendMessage(gson.toJson(categoryChannelShareCount), CATEGORY_CHANNEL_SHARE_COUNT_TOPIC)
        } else {
//            shareLogApiService.execute(channelInfo.id.toInt(), channelInfo.video_share_url)
        }
    }
}

data class CategoryChannelShareData(
    @SerializedName("user_id")
    val customerId: Long,
    @SerializedName("content_type")
    val contentType: String,
    @SerializedName("content_id")
    val contentId: Int,
    @SerializedName("shared_url")
    val sharedUrl: String,
    @SerializedName("device_type")
    val deviceType: Int = Constants.DEVICE_TYPE,
    @SerializedName("device_id")
    val deviceId: String = CommonPreference.getInstance().deviceId,
    @SerializedName("app_version")
    val appVersion: String = CommonPreference.getInstance().appVersionName,
    @SerializedName("date_time")
    val shareDateTime: String = currentDateTime,
)
