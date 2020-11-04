package com.banglalink.toffee.model

data class MyChannelSubscribeBean(
    val channelId: Int,
    val isSubscribed: Int,
    val subscriberCount: Int,
    val message: String,
    val messageType: String,
    val systemTime: String
)