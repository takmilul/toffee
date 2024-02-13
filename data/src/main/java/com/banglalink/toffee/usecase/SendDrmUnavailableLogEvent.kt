package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.DRM_UNAVAILABLE_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendDrmUnavailableLogEvent @Inject constructor(private val mPref: SessionPreference) {
    
    fun execute(sendToPubSub: Boolean = true) {
        PubSubMessageUtil.send(
            DrmUnavailableLogData().also {
                it.phoneNumber = mPref.phoneNumber.ifBlank { mPref.hePhoneNumber }
            },
            DRM_UNAVAILABLE_TOPIC
        )
    }
}

data class DrmUnavailableLogData(
    @SerializedName("lat")
    val lat: String = SessionPreference.getInstance().latitude,
    @SerializedName("lon")
    val lon: String = SessionPreference.getInstance().longitude,
    @SerializedName("device")
    val device: String = Build.MANUFACTURER,
    @SerializedName("deviceModel")
    val deviceModel: String = Build.MODEL,
) : PubSubBaseRequest() {
    override var phoneNumber: String
        get() = super.phoneNumber
        set(value) {
            super.phoneNumber = value
        }
}