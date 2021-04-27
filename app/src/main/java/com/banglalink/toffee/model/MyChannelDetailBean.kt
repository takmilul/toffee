package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MyChannelDetailBean(
    val code: Int,
    @SerializedName("details")
    val myChannelDetail: MyChannelDetail?,
    val isRated: Int = 0,
    val myRating: Int = 0,
    val ratingCount: Float = 0.0f,
    var subscriberCount: Long = 0,
    var formattedSubscriberCount: String? = null,
    val isOwner: Int = 0,
    @SerializedName("channel_owner_id")
    val channelOwnerId: Int = 0,
    var isSubscribed: Int = 0,
    val systemTime: String? = null
)