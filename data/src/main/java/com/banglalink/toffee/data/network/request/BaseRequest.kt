package com.banglalink.toffee.data.network.request

import android.os.Build
import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class BaseRequest(
    @SerialName("apiName")
    open val apiName: String
) {
    @SerialName("deviceId")
    val deviceId = CommonPreference.getInstance().deviceId
    @SerialName("product")
    val product: String = "PLAAS OTT API"
    @SerialName("version")
    val version: Double = 1.01
    @SerialName("appId")
    val appId: String = "NexViewersentTV"
    @SerialName("appSecurityCode")
    val appSecurityCode: String = "eee80f834a6e15b47db06fb70e75bada"
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
}