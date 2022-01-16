package com.banglalink.toffee

import com.banglalink.toffee.lib.BuildConfig

object Constants {
    const val DEVICE_TYPE = BuildConfig.DEVICE_TYPE

    const val LOGIN_ERROR = 103
    const val SHOULD_LOG = true
    const val USER_ACTIVITIES_LIMIT = 150
    const val DEVICE_ID_HEADER = "DEVICE-ID"
    const val OUTSIDE_OF_BD_ERROR_CODE = 403
    const val INVALID_REFERRAL_ERROR_CODE = -100
    const val MULTI_DEVICE_LOGIN_ERROR_CODE = 109
    const val UN_ETHICAL_ACTIVITIES_ERROR_CODE = 402
    const val CLIENT_API_HEADER = "CLIENT-API-HEADER"
    const val HE_SESSION_TOKEN_HEADER = "ENRICHMENT-SESSION-TOKEN"
    
    
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

    val HE_KEY = object : Any() {
        var t = 0
        override fun toString(): String {
            val buf = ByteArray(16)
            t = 1491333727
            buf[0] = (t ushr 17).toByte()
            t = -1906451371
            buf[1] = (t ushr 10).toByte()
            t = 1708768173
            buf[2] = (t ushr 24).toByte()
            t = -1952310088
            buf[3] = (t ushr 6).toByte()
            t = 1442244974
            buf[4] = (t ushr 9).toByte()
            t = 1916285900
            buf[5] = (t ushr 3).toByte()
            t = -1914384722
            buf[6] = (t ushr 5).toByte()
            t = 276207619
            buf[7] = (t ushr 12).toByte()
            t = -1161896255
            buf[8] = (t ushr 9).toByte()
            t = 745598406
            buf[9] = (t ushr 16).toByte()
            t = 1187200402
            buf[10] = (t ushr 20).toByte()
            t = 2050354488
            buf[11] = (t ushr 15).toByte()
            t = -626910532
            buf[12] = (t ushr 6).toByte()
            t = -384464058
            buf[13] = (t ushr 3).toByte()
            t = -428062792
            buf[14] = (t ushr 20).toByte()
            t = 682937630
            buf[15] = (t ushr 9).toByte()
            return String(buf)
        }
    }.toString()
}
