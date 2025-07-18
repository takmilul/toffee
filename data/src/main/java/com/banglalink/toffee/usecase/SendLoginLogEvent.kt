package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.LOGIN_LOG_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.util.currentDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class SendLoginLogEvent @Inject constructor() {
    
    fun execute(apiName: String, sendToPubSub: Boolean = true) {
        if (sendToPubSub) {
            PubSubMessageUtil.sendMessage(LoginLogData(apiName), LOGIN_LOG_TOPIC)
        }
    }
}

@Serializable
data class LoginLogData(
    @SerialName("api_name")
    val apiName: String,
    @SerialName("is_request_from_backend")
    val isRequestFromBackend: Int = 0,
    @SerialName("id")
    val id: Long = System.nanoTime(),
    @SerialName("app_version")
    val appVersion : String = CommonPreference.getInstance().appVersionName,
    @SerialName("device_type")
    val deviceType :String = "${Constants.DEVICE_TYPE}",
    @SerialName("device_id")
    val deviceId: String = CommonPreference.getInstance().deviceId,
    @SerialName("is_bl_number")
    val isBlNumber: Int = if(SessionPreference.getInstance().isBanglalinkNumber == "false") 0 else 1,
    @SerialName("lat")
    val lat: String = SessionPreference.getInstance().latitude,
    @SerialName("lon")
    val lon: String = SessionPreference.getInstance().longitude,
    @SerialName("geo_city")
    val geoCity: String = SessionPreference.getInstance().geoCity,
    @SerialName("geo_location")
    val geoLocation: String = SessionPreference.getInstance().geoLocation,
    @SerialName("user_ip")
    val userIp: String = SessionPreference.getInstance().userIp,
    @SerialName("net_type")
    val netType:String = SessionPreference.getInstance().netType,
    @SerialName("os_name")
    val osVersion :String = "android "+ Build.VERSION.RELEASE,
    @SerialName("parent_id")
    val parentId: Int = 1,
    @SerialName("session_token")
    val sessionToken: String = SessionPreference.getInstance().sessionToken,
    @SerialName("user_id")
    val customerId:Long = SessionPreference.getInstance().customerId.toLong(),
    @SerialName("msisdn")
    val msisdn :String = SessionPreference.getInstance().phoneNumber.toString(),
    @SerialName("date_time")
    val dateTime: String = currentDateTime,
    @SerialName("reportingTime")
    val reportingTime: String = currentDateTime
)