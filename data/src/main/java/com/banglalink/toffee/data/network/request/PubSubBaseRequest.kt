package com.banglalink.toffee.data.network.request

import android.os.Build
import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.util.currentDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class PubSubBaseRequest {
    @SerialName("deviceType")
    val deviceType: Int = Constants.DEVICE_TYPE
    @SerialName("appVersion")
    val appVersion: String = CommonPreference.getInstance().appVersionName
    @SerialName("osVersion")
    val osVersion: String = "android " + Build.VERSION.RELEASE
    @SerialName("netType")
    val netType: String = SessionPreference.getInstance().netType
    @SerialName("isBlNumber")
    open var isBlNumber = SessionPreference.getInstance().isBanglalinkNumber
    @SerialName("deviceId")
    val deviceId: String = CommonPreference.getInstance().deviceId
    @SerialName("customerId")
    val customerId: Long = SessionPreference.getInstance().customerId.toLong()
    @SerialName("msisdn")
    open var phoneNumber: String = SessionPreference.getInstance().phoneNumber
    @SerialName("reportingTime")
    val reportingTime: String = currentDateTime
}