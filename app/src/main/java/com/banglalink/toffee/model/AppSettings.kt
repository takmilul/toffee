package com.banglalink.toffee.model

import android.os.Build
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.google.android.exoplayer2.ExoPlayerLibraryInfo

const val LOGIN_ERROR = 103
const val USER_ACTIVITIES_LIMIT = 150
const val OUTSIDE_OF_BD_ERROR_CODE = 403
const val INVALID_REFERRAL_ERROR_CODE = -100
const val MULTI_DEVICE_LOGIN_ERROR_CODE = 109
const val UN_ETHICAL_ACTIVITIES_ERROR_CODE = 402
const val CLIENT_API_HEADER = "CLIENT-API-HEADER"

const val TOFFEE_BASE_URL = "https://mapi.toffeelive.com/"
//const val TOFFEE_BASE_URL = "https://staging.toffee-cms.com/"
//const val TOFFEE_BASE_URL = "https://ugc-staging.toffeelive.com/"

val TIME_OUT_MSG = "Time out occurred. Please try later" // Need to change the message later.
val EXIT_FROM_APP_MSG = "Are you sure to logout from the %s app?"
val SERVER_ERROR_MSG = "Server not responding right now. Please try later." // Need to change the message later.
val NO_INTERNET_MSG = "No internet found. Please check your internet settings" // Need to change the message later.

val TOFFEE_HEADER=("Toffee" + "/" + BuildConfig.VERSION_NAME + " (Linux;Android " + Build.VERSION.RELEASE + ") " + ExoPlayerLibraryInfo.VERSION_SLASHY + "/" + SessionPreference.getInstance().customerId + "/" + CommonPreference.getInstance().deviceId)

//    https://github.com/shamanland/simple-string-obfuscator
val TOFFEE_KEY = object : Any() {
    var t = 0
    override fun toString(): String {
        val buf = ByteArray(16)
        t = -1930939990
        buf[0] = (t ushr 8).toByte()
        t = -1504968233
        buf[1] = (t ushr 21).toByte()
        t = 130652637
        buf[2] = (t ushr 11).toByte()
        t = 1497997422
        buf[3] = (t ushr 11).toByte()
        t = -425192637
        buf[4] = (t ushr 21).toByte()
        t = 1653453717
        buf[5] = (t ushr 14).toByte()
        t = 1701321722
        buf[6] = (t ushr 7).toByte()
        t = -924612210
        buf[7] = (t ushr 12).toByte()
        t = -670853495
        buf[8] = (t ushr 12).toByte()
        t = 51655855
        buf[9] = (t ushr 20).toByte()
        t = 1537623876
        buf[10] = (t ushr 13).toByte()
        t = -1556092827
        buf[11] = (t ushr 20).toByte()
        t = 1293143540
        buf[12] = (t ushr 22).toByte()
        t = -662934916
        buf[13] = (t ushr 9).toByte()
        t = -121737930
        buf[14] = (t ushr 9).toByte()
        t = -958150212
        buf[15] = (t ushr 3).toByte()
        return String(buf)
    }
}.toString()