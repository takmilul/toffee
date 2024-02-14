package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.DRM_UNAVAILABLE_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class SendDrmUnavailableLogEvent @Inject constructor(
    private val mPref: SessionPreference
) {
    
    fun execute(sendToPubSub: Boolean = true) {
        PubSubMessageUtil.send(
            DrmUnavailableLogData().also {
                it.phoneNumber = mPref.phoneNumber.ifBlank { mPref.hePhoneNumber }
            },
            DRM_UNAVAILABLE_TOPIC
        )
    }
}

@Serializable
data class DrmUnavailableLogData(
    @SerialName("lat")
    val lat: String = SessionPreference.getInstance().latitude,
    @SerialName("lon")
    val lon: String = SessionPreference.getInstance().longitude,
    @SerialName("device")
    val device: String = Build.MANUFACTURER,
    @SerialName("deviceModel")
    val deviceModel: String = Build.MODEL,
) : PubSubBaseRequest() {
    override var phoneNumber: String
        get() = super.phoneNumber
        set(value) {
            super.phoneNumber = value
        }
}