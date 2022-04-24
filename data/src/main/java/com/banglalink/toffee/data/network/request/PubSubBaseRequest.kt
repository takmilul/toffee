package com.banglalink.toffee.data.network.request

import android.os.Build
import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.util.currentDateTime
import com.google.gson.annotations.SerializedName

open class PubSubBaseRequest {
    @SerializedName("deviceType")
    val deviceType: Int = Constants.DEVICE_TYPE
    @SerializedName("appVersion")
    val appVersion: String = CommonPreference.getInstance().appVersionName
    @SerializedName("osVersion")
    val osVersion: String = "android " + Build.VERSION.RELEASE
    @SerializedName("netType")
    val netType: String = SessionPreference.getInstance().netType
    @SerializedName("isBlNumber")
    open var isBlNumber = SessionPreference.getInstance().isBanglalinkNumber
    @SerializedName("deviceId")
    val deviceId: String = CommonPreference.getInstance().deviceId
    @SerializedName("customerId")
    val customerId: Long = SessionPreference.getInstance().customerId.toLong()
    @SerializedName("msisdn")
    open var phoneNumber: String = SessionPreference.getInstance().phoneNumber
    @SerializedName("reportingTime")
    val reportingTime: String = currentDateTime
}