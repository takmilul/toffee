package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class HeartBeatRequest(
    @SerializedName("contentId")
    val contentId: Int,
    @SerializedName("contentType")
    val contentType: String,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("lat")
    val lat: String,
    @SerializedName("lon")
    val lon: String,
    @SerializedName("isNetworkSwitch")
    val isNetworkSwitch: Boolean = false,
    @SerializedName("type")
    val type: String = "FOREGROUND"
) : BaseRequest("heartBeat")