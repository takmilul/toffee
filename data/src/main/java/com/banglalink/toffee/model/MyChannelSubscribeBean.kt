package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelSubscribeBean(
    @SerialName("channelId")
    val channelId: Int,
    @SerialName("isSubscribed")
    val isSubscribed: Int,
    @SerialName("subscriberCount")
    var subscriberCount: Long,
    @SerialName("message")
    val message: String,
    @SerialName("messageType")
    val messageType: String,
    @SerialName("systemTime")
    val systemTime: String
)