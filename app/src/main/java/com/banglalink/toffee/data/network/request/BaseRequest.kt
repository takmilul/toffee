package com.banglalink.toffee.data.network.request

import android.os.Build
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference

open class BaseRequest(val apiName: String) {
    val deviceId = CommonPreference.getInstance().deviceId
    val product : String = "PLAAS OTT API"
    val version : Double = 1.01
    val appId: String = "NexViewersentTV"
    val appSecurityCode : String = "eee80f834a6e15b47db06fb70e75bada"
    val deviceType :Int = 1
    val appVersion : String = BuildConfig.VERSION_NAME
    val osVersion :String = "android "+Build.VERSION.RELEASE
    val netType:String = SessionPreference.getInstance().netType
    val isBlNumber = SessionPreference.getInstance().isBanglalinkNumber
}