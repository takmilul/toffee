package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.DRM_FALLBACK_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.Gson
import javax.inject.Inject

class SendDrmFallbackEvent @Inject constructor(private val mPref: SessionPreference) {
    private val gson = Gson()
    
    fun execute(channelId: Long, reason: String) {
        PubSubMessageUtil.sendMessage(
            gson.toJson(DrmFallbackData(reason, channelId).also {
                it.phoneNumber = if (mPref.phoneNumber.isNotBlank()) mPref.phoneNumber else mPref.hePhoneNumber
            }),
            DRM_FALLBACK_TOPIC
        )
    }
}

data class DrmFallbackData(
    val reason: String,
    val channelId: Long,
    val lat: String = SessionPreference.getInstance().latitude,
    val lon: String = SessionPreference.getInstance().longitude,
    val deviceManufacturer: String = Build.MANUFACTURER,
    val deviceModel: String = Build.MODEL,
) : PubSubBaseRequest() {
    override var phoneNumber: String
        get() = super.phoneNumber
        set(value) {
            super.phoneNumber = value
        }
}