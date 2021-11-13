package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class UserChannelBean(
    @SerializedName("channels")
    val channels: List<UserChannelInfo>?,
    @SerializedName("subscriberCount")
    var subscriberCount: Int = 0,
    @SerializedName("count")
    val count: Int,
    @SerializedName("totalCount")
    val totalCount: Int = 0,
    @SerializedName("systemTime")
    val systemTime: String? = null
)