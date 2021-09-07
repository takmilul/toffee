package com.banglalink.toffee.data.network.request

import android.os.Build
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.google.gson.annotations.SerializedName

open class BaseRequest(val apiName: String) {
    @SerializedName("deviceId") val deviceId = CommonPreference.getInstance().deviceId
    @SerializedName("product") val product : String = "PLAAS OTT API"
    @SerializedName("version") val version : Double = 1.01
    @SerializedName("appId") val appId: String = "NexViewersentTV"
    @SerializedName("appSecurityCode") val appSecurityCode : String = "eee80f834a6e15b47db06fb70e75bada"
    @SerializedName("deviceType") val deviceType :Int = 1
    @SerializedName("appVersion") val appVersion : String = BuildConfig.VERSION_NAME
    @SerializedName("osVersion") val osVersion :String = "android "+Build.VERSION.RELEASE
    @SerializedName("netType") val netType:String = SessionPreference.getInstance().netType
    @SerializedName("isBlNumber") open var isBlNumber = SessionPreference.getInstance().isBanglalinkNumber
}