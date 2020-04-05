package com.banglalink.toffee.receiver

interface OtpReceiveListener {
    fun onOtpReceived(otp: String?)
    fun onOtpTimeout()
}