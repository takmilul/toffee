package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class ResponseBean(
    @SerializedName("systemTime") val systemTime: String,
    @SerializedName("message") val message: String,
    @SerializedName("messageType") val messageType: String
)