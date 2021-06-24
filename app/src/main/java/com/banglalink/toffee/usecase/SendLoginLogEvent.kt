package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.toFormattedDate
import com.banglalink.toffee.notification.LOGIN_LOG_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendLoginLogEvent @Inject constructor() {

    private val gson = Gson()
    
    fun execute(sendToPubSub: Boolean = true) {
        if (sendToPubSub) {
            PubSubMessageUtil.sendMessage(gson.toJson(LoginLogData()), LOGIN_LOG_TOPIC)
        }
    }
}

data class LoginLogData(
    val id: Long = System.nanoTime(),
    @SerializedName("app_version")
    val appVersion : String = BuildConfig.VERSION_NAME,
    @SerializedName("device_type")
    val deviceType :String = "1",
    @SerializedName("device_id")
    val deviceId: String = CommonPreference.getInstance().deviceId,
    @SerializedName("is_bl_number")
    val isBlNumber: Int = if(SessionPreference.getInstance().isBanglalinkNumber == "false") 0 else 1,
    val lat: String = SessionPreference.getInstance().latitude,
    val lon: String = SessionPreference.getInstance().longitude,
    @SerializedName("geo_city")
    val geoCity: String = SessionPreference.getInstance().geoCity,
    @SerializedName("geo_location")
    val geoLocation: String = SessionPreference.getInstance().geoLocation,
    @SerializedName("user_ip")
    val userIp: String = SessionPreference.getInstance().userIp,
    @SerializedName("net_type")
    val netType:String = SessionPreference.getInstance().netType,
    @SerializedName("os_name")
    val osVersion :String = "android "+ Build.VERSION.RELEASE,
    @SerializedName("parent_id")
    val parentId: Int = 1,
    @SerializedName("session_token")
    val sessionToken: String = SessionPreference.getInstance().sessionToken,
    @SerializedName("user_id")
    val customerId:Long = SessionPreference.getInstance().customerId.toLong(),
    @SerializedName("date_time")
    val dateTime: String = System.currentTimeMillis().toFormattedDate(),
)
