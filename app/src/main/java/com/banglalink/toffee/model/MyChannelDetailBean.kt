package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MyChannelDetailBean(
    val code: Int,
    @SerializedName("details")
    val myChannelDetail: MyChannelDetail?,
    val ratingCount: Float = 0.0f,
    var subscriberCount: String? = null,
    var formattedSubscriberCount: String? = null,
    val isOwner: Int = 0,
    val isSubscribed: Int = 0,
    val systemTime: String? = null
)