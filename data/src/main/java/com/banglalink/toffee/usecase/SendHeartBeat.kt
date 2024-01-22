package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.Constants
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.data.network.request.HeartBeatRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.ifNotNullOrBlank
import com.banglalink.toffee.extension.ifNotNullOrEmpty
import com.banglalink.toffee.notification.HEARTBEAT_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.util.currentDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SendHeartBeat @Inject constructor(
    private val json: Json,
    private val preference: SessionPreference, private val toffeeApi: ToffeeApi
) {
    
    suspend fun execute(
        contentId: Int,
        contentType: String,
        dataSource: String,
        ownerId: Int,
        isNetworkSwitch: Boolean = false,
        sendToPubSub: Boolean = true
    ) {
        withContext(Dispatchers.IO) {
            if (!sendToPubSub && preference.customerId > 0) {
                sendToToffeeServer(contentId, contentType, dataSource, ownerId, isNetworkSwitch)
            } else {
                sendToPubSub(contentId, contentType, dataSource, ownerId)
            }
        }
    }
    
    private fun sendToPubSub(contentId: Int, contentType: String, dataSource: String, ownerId: Int) {
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
        PubSubMessageUtil.sendMessage(json.encodeToString(heartBeatData), HEARTBEAT_TOPIC)
    }
    
    private suspend fun sendToToffeeServer(contentId: Int, contentType: String, dataSource: String, ownerId: Int, isNetworkSwitch: 
    Boolean = false) {
        var needToRefreshSessionToken = isNetworkSwitch
        if (System.currentTimeMillis() - preference.getSessionTokenSaveTimeInMillis() > preference.getSessionTokenLifeSpanInMillis()) {
            needToRefreshSessionToken = true// we need to refresh token by setting isNetworkSwitch = true
        }
        val response = tryIO {
            toffeeApi.sendHeartBeat(
                preference.getDBVersionByApiName(ApiNames.SEND_HEART_BEAT),
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
        response.response.dbVersionList?.ifNotNullOrEmpty {
            preference.setDBVersion(it.toList())
        }
        response.response.sessionToken?.ifNotNullOrBlank {
            preference.sessionToken = it
        }
        response.response.headerSessionToken?.ifNotNullOrBlank {
            preference.setHeaderSessionToken(it)
        }
        response.response.systemTime?.ifNotNullOrBlank {
            preference.setSystemTime(it)
        }
    }
    
    @Serializable
    private data class HeartBeatData(
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
        val ownerId: Int = 0,
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