package com.banglalink.toffee.usecase

import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.network.request.ViewingContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.ActivityType
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.VIEWCONTENT_TOPIC
import com.banglalink.toffee.util.Utils
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendViewContentEvent @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val activityRepo: UserActivitiesRepository
) {

    private val gson = Gson()



    suspend fun execute(channel: ChannelInfo, sendToPubSub:Boolean = true){
        if(sendToPubSub){
            sendToPubSub(channel.id.toInt(),channel.type ?: "")
        }
        else{
            sendToToffeeServer(channel.id.toInt(),channel.type ?: "")
        }
        saveToLocalDb(channel)
    }

    private suspend fun saveToLocalDb(channel: ChannelInfo){
        val channelDataModel = UserActivities(
            preference.customerId,
            channel.id.toLong(),
            "history",
            channel.type ?: "",
            gson.toJson(channel),
            ActivityType.WATCHED.value,
            Reaction.Watched.value
        )
        activityRepo.insert(channelDataModel)
    }

    private fun sendToPubSub(contentId: Int,contentType: String){
        val viewContentData = ViewContentData(
            customerId = preference.customerId,
            contentId = contentId,
            contentType = contentType,
            latitude = preference.latitude,
            longitude = preference.longitude,
            isBlNumber = if (preference.isBanglalinkNumber == "true") 1 else 0,
            netType = preference.netType,
            sessionToken = preference.getHeaderSessionToken()?:""
        )
        PubSubMessageUtil.sendMessage(gson.toJson(viewContentData), VIEWCONTENT_TOPIC)
    }

    private suspend fun sendToToffeeServer(contentId: Int,contentType: String){
        tryIO2 {
            toffeeApi.sendViewingContent(
                ViewingContentRequest(
                    contentType,
                    contentId,
                    preference.customerId,
                    preference.password,
                    preference.latitude,
                    preference.longitude
                )
            )
        }
    }


    private data class ViewContentData(
        @SerializedName("id")
        val id: Long = System.nanoTime(),
        @SerializedName("customer_id")
        val customerId: Int,
        @SerializedName("device_type")
        val deviceType: Int = Constants.DEVICE_TYPE,
        @SerializedName("content_id")
        val contentId: Int,
        @SerializedName("content_type")
        val contentType: String,
        @SerializedName("lat")
        val latitude: String,
        @SerializedName("lon")
        val longitude: String,
        @SerializedName("os_name")
        val os: String = "android",
        @SerializedName("app_version")
        val appVersion: String = CommonPreference.getInstance().appVersionName,
        @SerializedName("is_bl_number")
        val isBlNumber: Int,
        @SerializedName("net_type")
        val netType: String,
        @SerializedName("session_token")
        val sessionToken: String,
        @SerializedName("device_id")
        val deviceId: String = CommonPreference.getInstance().deviceId,
        @SerializedName("date_time")
        val dateTime: String = Utils.getDateTime()
    )
}