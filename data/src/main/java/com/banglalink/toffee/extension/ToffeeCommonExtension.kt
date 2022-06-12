package com.banglalink.toffee.extension

import android.text.TextUtils
import java.net.MalformedURLException
import java.net.URL
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

fun ByteArray.toHex() = joinToString(separator = "") { byte -> "%02x".format(byte) }

fun Long.toFormattedDate(): String{
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Dhaka"))
    val cal = Calendar.getInstance(TimeZone.getDefault())
    cal.timeInMillis = this
    val dateGMT = cal.time
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    return sdf.format(dateGMT)
}

fun Long.toFormattedDateMillis(): String{
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Dhaka"))
    val cal = Calendar.getInstance(TimeZone.getDefault())
    cal.timeInMillis = this
    val dateGMT = cal.time
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    return sdf.format(dateGMT)
}

fun String.toMD5(): String {
    return try {
        val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
        bytes.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        ""
    }
}

inline fun String?.ifNotBlank(function: ((it: String) -> Unit)) {
    if (!this.isNullOrBlank()) {
        function(this)
    }
}

inline fun String?.isNotBlank(function: ((it: String) -> String?)): String? {
    return if (!this.isNullOrBlank()) {
        function(this)
    } else null
}

fun String.overrideUrl(newUrl: String?): String {
    return newUrl?.isNotBlank {
        try {
            val url = URL(this)
            var path = url.path
            if (!TextUtils.isEmpty(url.query)) {
                path = path + "?" + url.query
            }
            newUrl + path
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            this
        }
    } ?: this
}