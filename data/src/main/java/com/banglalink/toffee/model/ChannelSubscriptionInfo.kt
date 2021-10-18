package com.banglalink.toffee.model

data class ChannelSubscriptionInfo(
    val channelName: String,
    val channelLogo: String,
    val isLive: Boolean = false,
    val viewCount: String = "528K",
    var notificationStatus: Boolean = false,
    val notificationCount: Int = 0,
    val subscriptionStatus: Boolean = false,
    val subscriptionAmount: String? = null,
    val validTill: Long = 0L,
    val formattedViewCount: String? = null
)