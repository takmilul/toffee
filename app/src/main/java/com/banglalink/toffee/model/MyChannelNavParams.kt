package com.banglalink.toffee.model

data class MyChannelNavParams(
    val channelId: Int, 
    val channelOwnerId: Int, 
    val isSubscribed: Int,
    val pageTitle: String = ""
)