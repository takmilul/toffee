package com.banglalink.toffee.model

data class SubscribeChannelBean(
    val channelId: Int,
    val isSubscribed: Int,
    val message: String,
    val messageType: String,
    val systemTime: String
)