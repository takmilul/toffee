package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.network.request.ViewingContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.ActivityType
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.VIEW_CONTENT_TOPIC
import com.banglalink.toffee.util.currentDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SendViewContentEvent @Inject constructor(
    private val json: Json,
    private val toffeeApi: ToffeeApi,
    private val preference: SessionPreference,
    private val activityRepo: UserActivitiesRepository
) {
    
    suspend fun execute(channelInfo: ChannelInfo, sendToPubSub: Boolean = true) {
        if (sendToPubSub) {
            val contentId = channelInfo.getContentId()
            sendToPubSub(contentId.toInt(), channelInfo.type ?: "0", channelInfo.dataSource ?: "iptv_programs", channelInfo.channel_owner_id.toString())
        } else {
            sendToToffeeServer(channelInfo.id.toInt(), channelInfo.type ?: "0", channelInfo.dataSource ?: "iptv_programs", channelInfo.channel_owner_id.toString())
        }
        saveToLocalDb(channelInfo)
    }
    
    private suspend fun saveToLocalDb(channel: ChannelInfo) {
        val channelDataModel = UserActivities(
            preference.customerId,
            channel.id.toLong(),
            "history",
            channel.type ?: "",
            json.encodeToString(channel),
            ActivityType.WATCHED.value,
            Reaction.Watched.value
        )
        activityRepo.insert(channelDataModel)
    }
    
    private fun sendToPubSub(contentId: Int, contentType: String, dataSource: String, ownerId: String) {
        val viewContentData = ViewContentData(
            customerId = preference.customerId,
            contentId = contentId,
            contentType = contentType,
            dataSource = dataSource,
            ownerId = ownerId,
            latitude = preference.latitude,
            longitude = preference.longitude,
            isBlNumber = if (preference.isBanglalinkNumber == "true") 1 else 0,
            netType = preference.netType,
            sessionToken = preference.getHeaderSessionToken() ?: ""
        )
        PubSubMessageUtil.sendMessage(json.encodeToString(viewContentData), VIEW_CONTENT_TOPIC)
    }
    
    private suspend fun sendToToffeeServer(contentId: Int, contentType: String, dataSource: String, ownerId: String) {
        tryIO {
            toffeeApi.sendViewingContent(
                ViewingContentRequest(
                    contentType, contentId, dataSource, ownerId, preference.customerId, preference.password, preference.latitude, preference.longitude
                )
            )
        }
    }
    @Serializable
    private data class ViewContentData(
        @SerialName("id")
        val id: Long = System.nanoTime(),
        @SerialName("customer_id")
        val customerId: Int,
        @SerialName("device_type")
        val deviceType: Int = Constants.DEVICE_TYPE,
        @SerialName("content_id")
        val contentId: Int,
        @SerialName("content_type")
        val contentType: String,
        @SerialName("data_source")
        val dataSource: String? = "iptv_programs",
        @SerialName("channel_owner_id")
        val ownerId: String? = "0",
        @SerialName("lat")
        val latitude: String,
        @SerialName("lon")
        val longitude: String,
        @SerialName("os_name")
        val os: String = "android " + Build.VERSION.RELEASE,
        @SerialName("app_version")
        val appVersion: String = CommonPreference.getInstance().appVersionName,
        @SerialName("is_bl_number")
        val isBlNumber: Int,
        @SerialName("net_type")
        val netType: String,
        @SerialName("session_token")
        val sessionToken: String,
        @SerialName("device_id")
        val deviceId: String = CommonPreference.getInstance().deviceId,
        @SerialName("date_time")
        val dateTime: String = currentDateTime,
        @SerialName("reportingTime")
        val reportingTime: String = currentDateTime
    )
}