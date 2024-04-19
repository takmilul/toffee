package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.LOGOUT_LOG_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class SendLogOutLogEvent @Inject constructor() {
    
    fun execute(sendToPubSub: Boolean = true) {
        if (sendToPubSub) {
            PubSubMessageUtil.sendMessage(LogOutLogEvent(), LOGOUT_LOG_TOPIC)
        }
    }
}

@Serializable
data class LogOutLogEvent(
    @SerialName("id")
    val id: Long = System.nanoTime(),
    @SerialName("customerIp")
    val customerIp: String=SessionPreference.getInstance().userIp,
    @SerialName("lat")
    val lat: String = SessionPreference.getInstance().latitude,
    @SerialName("lon")
    val lon: String = SessionPreference.getInstance().longitude,
    @SerialName("geoCity")
    val geoCity: String = SessionPreference.getInstance().geoCity,
    @SerialName("geoLocation")
    val geoLocation: String = SessionPreference.getInstance().geoLocation,
    @SerialName("is_request_from_backend")
    val isRequestFromBackend : Int = 0,
): PubSubBaseRequest()