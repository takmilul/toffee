package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.USER_OTP_TOPIC
import com.google.gson.Gson
import javax.inject.Inject

class SendOTPLogEvent @Inject constructor() {
    private val gson = Gson()
    
    fun execute(otpLogData: OTPLogData, phoneNumber: String, sendToPubSub: Boolean = true) {
        otpLogData.phoneNumber = phoneNumber
        PubSubMessageUtil.sendMessage(gson.toJson(otpLogData), USER_OTP_TOPIC)
    }
}

data class OTPLogData(
    val otp: String = "",
    val isRequested: Int = 0,
    val isReceived: Int = 0,
    val isUsed: Int = 0,
): PubSubBaseRequest(){
    override var phoneNumber: String
        get() = super.phoneNumber
        set(value) {super.phoneNumber = value}
}