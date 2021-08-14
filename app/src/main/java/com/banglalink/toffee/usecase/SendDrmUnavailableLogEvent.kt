package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.DRM_UNAVAILABLE_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.Gson
import javax.inject.Inject

class SendDrmUnavailableLogEvent @Inject constructor() {
    private val gson = Gson()
    
    fun execute(sendToPubSub: Boolean = true) {
        PubSubMessageUtil.sendMessage(gson.toJson(DrmUnavailableLogData()), DRM_UNAVAILABLE_TOPIC)
    }
}

data class DrmUnavailableLogData(
    val deviceManufacturer: String = Build.MANUFACTURER,
    val deviceModel: String = Build.MODEL,
): PubSubBaseRequest()