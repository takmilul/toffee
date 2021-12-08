package com.banglalink.toffee.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.util.SingleLiveEvent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.lang.Exception

class SMSBroadcastReceiver : BroadcastReceiver() {

    private val _otpLiveData = SingleLiveEvent<String>();
    val otpLiveData = _otpLiveData.toLiveData()

    private val TAG = "SMSBroadcastReceiver"
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
                val extras: Bundle? = intent.extras
                val mStatus =
                    extras?.get(SmsRetriever.EXTRA_STATUS) as Status?
                when (mStatus!!.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        // Get SMS message contents'
                        val message =
                            extras?.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String?
                        Log.i(TAG, "onReceive: failure $message")
                        val start = message!!.indexOf(":") + 1
                        val otp = message.substring(start, start + 6)
                        _otpLiveData.postValue(otp)
                    }
                    CommonStatusCodes.TIMEOUT -> {
                        // Waiting for SMS timed out (5 minutes)
                        Log.i(TAG, "onReceive: failure")
                    }
                }
            }
        }catch (e:Exception){
            ToffeeAnalytics.logException(e)
            ToffeeAnalytics.logBreadCrumb("Error in sms broadcast receiver")
        }

    }
}