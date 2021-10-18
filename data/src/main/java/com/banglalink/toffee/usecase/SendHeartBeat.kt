package com.banglalink.toffee.usecase

import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.network.request.HeartBeatRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.HEARTBEAT_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.util.Utils
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendHeartBeat @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi
) {

    private val gson = Gson()
    suspend fun execute(
        contentId: Int,
        contentType: String,
        isNetworkSwitch: Boolean = false,
        sendToPubSub: Boolean = true
    ) {
        withContext(Dispatchers.IO){
            if (sendToPubSub) {
                sendToPubSub(contentId, contentType)
            } else {
                sendToToffeeServer(contentId, contentType, isNetworkSwitch)
            }
        }
    }

    private fun sendToPubSub(contentId: Int, contentType: String) {
        val heartBeatData = HeartBeatData(
            customerId = preference.customerId,
            contentId = contentId,
            contentType = contentType,
            latitude = preference.latitude,
            longitude = preference.longitude,
            isBlNumber = if (preference.isBanglalinkNumber == "true") 1 else 0,
            netType = preference.netType,
            sessionToken = preference.getHeaderSessionToken()?:""
        )
        PubSubMessageUtil.sendMessage(gson.toJson(heartBeatData), HEARTBEAT_TOPIC)
    }

    private suspend fun sendToToffeeServer(
        contentId: Int,
        contentType: String,
        isNetworkSwitch: Boolean = false
    ) {
        var needToRefreshSessionToken = isNetworkSwitch
        if (System.currentTimeMillis() - preference.getSessionTokenSaveTimeInMillis() > preference.getSessionTokenLifeSpanInMillis()) {
            needToRefreshSessionToken =
                true// we need to refresh token by setting isNetworkSwitch = true
        }
        val response = tryIO2 {
            toffeeApi.sendHeartBeat(
                HeartBeatRequest(
                    contentId,
                    contentType,
                    preference.customerId,
                    preference.password,
                    preference.latitude,
                    preference.longitude,
                    isNetworkSwitch = needToRefreshSessionToken
                )
            )
        }
        preference.sessionToken = response.response.sessionToken ?: ""
        preference.setHeaderSessionToken(response.response.headerSessionToken)
        response.response.systemTime?.let {
            preference.setSystemTime(it)
        }
    }

    private data class HeartBeatData(
        @SerializedName("id")
        val id: Long = System.nanoTime(),
        @SerializedName("customer_id")
        val customerId: Int,
        @SerializedName("device_type")
        val deviceType: Int = 1,
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
        val appVersion: String = Constants.VERSION_NAME,
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