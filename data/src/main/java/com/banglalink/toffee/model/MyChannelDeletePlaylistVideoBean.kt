package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MyChannelDeletePlaylistVideoBean(
    @SerializedName("message")
    val message: String,
    @SerializedName("messageType")
    val messageType: String,
    @SerializedName("systemTime")
    val systemTime: String
)