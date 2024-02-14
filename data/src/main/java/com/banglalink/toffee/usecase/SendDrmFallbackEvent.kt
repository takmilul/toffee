package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.DRM_FALLBACK_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class SendDrmFallbackEvent @Inject constructor(
    private val mPref: SessionPreference
) {
    
    fun execute(channelId: Long, reason: String) {
        PubSubMessageUtil.send(
            DrmFallbackData(reason, channelId).also {
                it.phoneNumber = mPref.phoneNumber.ifBlank { mPref.hePhoneNumber }
            },
            DRM_FALLBACK_TOPIC
        )
    }
}

@Serializable
data class DrmFallbackData(
    @SerialName("reason")
    val reason: String? = null,
    @SerialName("channelId")
    val channelId: Long = 0,
    @SerialName("lat")
    val lat: String = SessionPreference.getInstance().latitude,
    @SerialName("lon")
    val lon: String = SessionPreference.getInstance().longitude,
    @SerialName("deviceManufacturer")
    val deviceManufacturer: String = Build.MANUFACTURER,
    @SerialName("deviceModel")
    val deviceModel: String = Build.MODEL,
) : PubSubBaseRequest() {
    override var phoneNumber: String
        get() = super.phoneNumber
        set(value) {
            super.phoneNumber = value
        }
}