package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.DRM_FALLBACK_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendDrmFallbackEvent @Inject constructor(private val mPref: SessionPreference) {
    
    fun execute(channelId: Long, reason: String) {
        PubSubMessageUtil.send(
            DrmFallbackData(reason, channelId).also {
                it.phoneNumber = mPref.phoneNumber.ifBlank { mPref.hePhoneNumber }
            },
            DRM_FALLBACK_TOPIC
        )
    }
}

data class DrmFallbackData(
    @SerializedName("reason")
    val reason: String,
    @SerializedName("channelId")
    val channelId: Long,
    @SerializedName("lat")
    val lat: String = SessionPreference.getInstance().latitude,
    @SerializedName("lon")
    val lon: String = SessionPreference.getInstance().longitude,
    @SerializedName("deviceManufacturer")
    val deviceManufacturer: String = Build.MANUFACTURER,
    @SerializedName("deviceModel")
    val deviceModel: String = Build.MODEL,
) : PubSubBaseRequest() {
    override var phoneNumber: String
        get() = super.phoneNumber
        set(value) {
            super.phoneNumber = value
        }
}