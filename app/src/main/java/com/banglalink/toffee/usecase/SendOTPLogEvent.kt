package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.USER_OTP_TOPIC
import com.google.gson.Gson
import javax.inject.Inject

class SendOTPLogEvent @Inject constructor(
    private val preference: SessionPreference,
) {
    
    private val gson = Gson()
    
    suspend fun execute(otpLogData: OTPLogData, sendToPubSub: Boolean = true) {
        PubSubMessageUtil.sendMessage(gson.toJson(otpLogData), USER_OTP_TOPIC)
    }
}

data class OTPLogData(
    val otp: String,
    val isReceived: Int,
    val isUsed: Int,
): PubSubBaseRequest()
