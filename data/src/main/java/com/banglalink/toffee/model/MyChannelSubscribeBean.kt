package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelSubscribeBean(
    @SerialName("channelId")
    val channelId: Int = 0,
    @SerialName("isSubscribed")
    val isSubscribed: Int = 0,
    @SerialName("subscriberCount")
    var subscriberCount: Long = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("messageType")
    val messageType: String? = null,
    @SerialName("systemTime")
    val systemTime: String? = null
)