package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MyChannelSubscribeBean(
    @SerializedName("channelId")
    val channelId: Int,
    @SerializedName("isSubscribed")
    val isSubscribed: Int,
    @SerializedName("subscriberCount")
    val subscriberCount: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("messageType")
    val messageType: String,
    @SerializedName("systemTime")
    val systemTime: String
)