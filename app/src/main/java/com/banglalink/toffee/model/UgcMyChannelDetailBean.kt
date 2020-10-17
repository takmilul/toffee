package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class UgcMyChannelDetailBean (
    val code: Int,
    @SerializedName("details")
    val myChannelDetail: UgcMyChannelDetail?,
    val ratingCount: Long? = 0,
    val subscriberCount: Long? = 0,
    val isOwner: Int = 0,
    val isSubscribed: Int = 0,
    val systemTime: String? = null
)