package com.banglalink.toffee.data.network.request

import android.os.Build
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.util.Utils
import com.google.gson.annotations.SerializedName

open class PubSubBaseRequest {
    val deviceType :Int = 1
    val appVersion : String = BuildConfig.VERSION_NAME
    val osVersion :String = "android "+Build.VERSION.RELEASE
    val netType:String = SessionPreference.getInstance().netType
    val isBlNumber = SessionPreference.getInstance().isBanglalinkNumber

    @SerializedName("deviceId")
    val deviceId: String = CommonPreference.getInstance().deviceId

    @SerializedName("customerId")
    val customerId:Long = SessionPreference.getInstance().customerId.toLong()

    @SerializedName("msisdn")
    open var phoneNumber: String = SessionPreference.getInstance().phoneNumber

    @SerializedName("reportingTime")
    val reportingTime = Utils.getDateTime()
}