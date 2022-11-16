package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.network.request.HeartBeatRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.doIfNotNullOrEmpty
import com.banglalink.toffee.notification.HEARTBEAT_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.util.currentDateTime
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendHeartBeat @Inject constructor(
    private val preference: SessionPreference, private val toffeeApi: ToffeeApi
) {
    
    private val gson = Gson()
    
    suspend fun execute(contentId: Int, contentType: String, dataSource: String, ownerId: String, isNetworkSwitch: Boolean = false, sendToPubSub: Boolean = true) {
        withContext(Dispatchers.IO) {
            if (sendToPubSub) {
                sendToPubSub(contentId, contentType, dataSource, ownerId)
            } else {
                sendToToffeeServer(contentId, contentType, dataSource, ownerId, isNetworkSwitch)
            }
        }
    }
    
    private fun sendToPubSub(contentId: Int, contentType: String, dataSource: String, ownerId: String) {
        val heartBeatData = HeartBeatData(
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
        PubSubMessageUtil.sendMessage(gson.toJson(heartBeatData), HEARTBEAT_TOPIC)
    }
    
    private suspend fun sendToToffeeServer(contentId: Int, contentType: String, dataSource: String, ownerId: String, isNetworkSwitch: Boolean = false) {
        var needToRefreshSessionToken = isNetworkSwitch
        if (System.currentTimeMillis() - preference.getSessionTokenSaveTimeInMillis() > preference.getSessionTokenLifeSpanInMillis()) {
            needToRefreshSessionToken = true// we need to refresh token by setting isNetworkSwitch = true
        }
        val response = tryIO2 {
            toffeeApi.sendHeartBeat(
                HeartBeatRequest(
                    contentId,
                    contentType,
                    dataSource,
                    ownerId,
                    preference.customerId,
                    preference.password,
                    preference.latitude,
                    preference.longitude,
                    isNetworkSwitch = needToRefreshSessionToken
                )
            )
        }
        preference.mqttIsActive = response.response.mqttIsActive == 1
        response.response.sessionToken?.doIfNotNullOrEmpty {
            preference.sessionToken = it
        }
        response.response.headerSessionToken?.doIfNotNullOrEmpty {
            preference.setHeaderSessionToken(it)
        }
        response.response.systemTime?.doIfNotNullOrEmpty {
            preference.setSystemTime(it)
        }
    }
    
    private data class HeartBeatData(
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
        @SerializedName("data_source")
        val dataSource: String? = "iptv_programs",
        @SerializedName("channel_owner_id")
        val ownerId: String? = "0",
        @SerializedName("lat")
        val latitude: String,
        @SerializedName("lon")
        val longitude: String,
        @SerializedName("os_name")
        val os: String = "android " + Build.VERSION.RELEASE,
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
        val dateTime: String = currentDateTime
    )
}