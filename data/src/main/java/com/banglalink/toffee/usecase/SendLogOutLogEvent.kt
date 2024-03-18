package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.LOGIN_LOG_TOPIC
import com.banglalink.toffee.notification.LOGOUT_LOG_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.util.currentDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class SendLogOutLogEvent @Inject constructor() {
    
    fun execute(sendToPubSub: Boolean = true) {
        if (sendToPubSub) {
            PubSubMessageUtil.send(LogOutLogEvent(), LOGOUT_LOG_TOPIC)
        }
    }
}

@Serializable
data class LogOutLogEvent(
@SerialName("id") val id: Long = System.nanoTime(),
@SerialName("customerIp") val customerIp: String=SessionPreference.getInstance().userIp,
@SerialName("lat") val lat: String = SessionPreference.getInstance().latitude,
@SerialName("lon")  val lon: String = SessionPreference.getInstance().longitude,
@SerialName("geoCity") val geoCity: String = SessionPreference.getInstance().geoCity,
@SerialName("geoLocation") val geoLocation: String = SessionPreference.getInstance().geoLocation,
@SerialName("isRequestFromBackend ") val isRequestFromBackend : Int = 0,

): PubSubBaseRequest()
