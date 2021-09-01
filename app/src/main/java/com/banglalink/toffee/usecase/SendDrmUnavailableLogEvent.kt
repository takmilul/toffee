package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.DRM_UNAVAILABLE_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.Gson
import javax.inject.Inject

class SendDrmUnavailableLogEvent @Inject constructor(private val mPref: SessionPreference) {
    private val gson = Gson()
    
    fun execute(sendToPubSub: Boolean = true) {
        PubSubMessageUtil.sendMessage(
            gson.toJson(DrmUnavailableLogData().also {
                it.phoneNumber = if (mPref.phoneNumber.isNotBlank()) mPref.phoneNumber else mPref.hePhoneNumber
            }),
            DRM_UNAVAILABLE_TOPIC
        )
    }
}

data class DrmUnavailableLogData(
    val lat: String = SessionPreference.getInstance().latitude,
    val lon: String = SessionPreference.getInstance().longitude,
    val device: String = Build.MANUFACTURER,
    val deviceModel: String = Build.MODEL,
) : PubSubBaseRequest() {
    override var phoneNumber: String
        get() = super.phoneNumber
        set(value) {
            super.phoneNumber = value
        }
}