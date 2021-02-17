package com.banglalink.toffee.data.network.request

import android.os.Build
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.util.Utils
import com.google.gson.annotations.SerializedName

open class PubSubBaseRequest {
    val deviceType :Int = 1
    val appVersion : String = BuildConfig.VERSION_NAME
    val osVersion :String = "android "+Build.VERSION.RELEASE
    val netType:String = Preference.getInstance().netType
    val isBlNumber = Preference.getInstance().isBanglalinkNumber

    @SerializedName("deviceId")
    val deviceId: String = Preference.getInstance().deviceId

    @SerializedName("customerId")
    val customerId:Long = Preference.getInstance().customerId.toLong()

    @SerializedName("msisdn")
    val phoneNumber: String = Preference.getInstance().phoneNumber

    @SerializedName("reportingTime")
    val reportingTime = Utils.getDateTime()
}