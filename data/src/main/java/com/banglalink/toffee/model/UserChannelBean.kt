package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class UserChannelBean(
    @SerializedName("channels")
    val channels: List<UserChannelInfo>?,
    var subscriberCount: Int = 0,
    val count: Int,
    val totalCount: Int = 0,
    val systemTime: String?=null
)