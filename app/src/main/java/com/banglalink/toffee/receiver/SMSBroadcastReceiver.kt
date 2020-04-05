package com.banglalink.toffee.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class SMSBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "SMSBroadcastReceiver"
    var otpReceiveInterface: OtpReceiveListener? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: ")
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
            val extras: Bundle? = intent?.extras
            val mStatus =
                extras?.get(SmsRetriever.EXTRA_STATUS) as Status?
            when (mStatus!!.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    // Get SMS message contents'
                    val message =
                        extras?.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String?
                    Log.d(TAG, "onReceive: failure $message")
                    if (otpReceiveInterface != null) {
                        var start = message!!.indexOf(":") + 1;
                        val otp = message!!.substring(start, start + 6)
                        otpReceiveInterface!!.onOtpReceived(otp)
                    }
                }
                CommonStatusCodes.TIMEOUT -> {
                    // Waiting for SMS timed out (5 minutes)
                    Log.d(TAG, "onReceive: failure")
                    otpReceiveInterface?.onOtpTimeout()
                }
            }
        }
    }

    fun setOnOtpListeners(otpReceiveInterface: OtpReceiveListener?) {
        this.otpReceiveInterface = otpReceiveInterface
    }
}