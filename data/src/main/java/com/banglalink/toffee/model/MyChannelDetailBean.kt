package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelDetailBean(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("details")
    val myChannelDetail: MyChannelDetail? = null,
    @SerialName("isRated")
    val isRated: Int = 0,
    @SerialName("myRating")
    val myRating: Int = 0,
    @SerialName("ratingCount")
    val ratingCount: Float = 0.0f,
    @SerialName("subscriberCount")
    var subscriberCount: Long = 0,
    @SerialName("formattedSubscriberCount")
    var formattedSubscriberCount: String? = null,
    @SerialName("isOwner")
    val isOwner: Int = 0,
    @SerialName("channel_owner_id")
    val channelOwnerId: Int = 0,
    @SerialName("isSubscribed")
    var isSubscribed: Int = 0,
    @SerialName("systemTime")
    val systemTime: String? = null
)