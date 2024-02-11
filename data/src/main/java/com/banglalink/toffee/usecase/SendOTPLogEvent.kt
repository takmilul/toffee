package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.USER_OTP_TOPIC
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendOTPLogEvent @Inject constructor() {
    
    fun execute(otpLogData: OTPLogData, phoneNumber: String, sendToPubSub: Boolean = true) {
        otpLogData.phoneNumber = phoneNumber
        PubSubMessageUtil.send(otpLogData, USER_OTP_TOPIC)
    }
}

data class OTPLogData(
    @SerializedName("otp")
    val otp: String = "",
    @SerializedName("isRequested")
    val isRequested: Int = 0,
    @SerializedName("isReceived")
    val isReceived: Int = 0,
    @SerializedName("isUsed")
    val isUsed: Int = 0,
): PubSubBaseRequest(){
    override var phoneNumber: String
        get() = super.phoneNumber
        set(value) {super.phoneNumber = value}
}