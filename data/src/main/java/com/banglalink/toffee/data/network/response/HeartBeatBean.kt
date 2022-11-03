package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class HeartBeatBean(
    @SerializedName("mqttIsActive")
    val mqttIsActive: Int,
    @SerializedName("systemTime")
    val systemTime: String? = null,
    @SerializedName("sessionToken")
    val sessionToken: String? = null,
    @SerializedName("headerSessionToken")
    val headerSessionToken: String? = null,
): BaseResponse()