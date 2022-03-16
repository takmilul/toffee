package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MyChannelDetailBean(
    @SerializedName("code")
    val code: Int,
    @SerializedName("details")
    val myChannelDetail: MyChannelDetail?,
    @SerializedName("isRated")
    val isRated: Int = 0,
    @SerializedName("myRating")
    val myRating: Int = 0,
    @SerializedName("ratingCount")
    val ratingCount: Float = 0.0f,
    @SerializedName("subscriberCount")
    var subscriberCount: Long = 0,
    @SerializedName("formattedSubscriberCount")
    var formattedSubscriberCount: String? = null,
    @SerializedName("isOwner")
    val isOwner: Int = 0,
    @SerializedName("channel_owner_id")
    val channelOwnerId: Int = 0,
    @SerializedName("isSubscribed")
    var isSubscribed: Int = 0,
    @SerializedName("systemTime")
    val systemTime: String? = null
)