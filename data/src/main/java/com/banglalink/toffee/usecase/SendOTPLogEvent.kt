package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.USER_OTP_TOPIC
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class SendOTPLogEvent @Inject constructor() {
    
    fun execute(otpLogData: OTPLogData, phoneNumber: String, sendToPubSub: Boolean = true) {
        otpLogData.phoneNumber = phoneNumber
        PubSubMessageUtil.send(otpLogData, USER_OTP_TOPIC)
    }
}

@Serializable
data class OTPLogData(
    @SerialName("otp")
    val otp: String = "",
    @SerialName("isRequested")
    val isRequested: Int = 0,
    @SerialName("isReceived")
    val isReceived: Int = 0,
    @SerialName("isUsed")
    val isUsed: Int = 0,
): PubSubBaseRequest(){
    override var phoneNumber: String
        get() = super.phoneNumber
        set(value) {super.phoneNumber = value}
}