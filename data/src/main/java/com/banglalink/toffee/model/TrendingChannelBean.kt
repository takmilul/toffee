package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class TrendingChannelBean(
    @SerializedName("channels")
    val channels: List<UserChannelInfo>?,
    @SerializedName("code")
    var code: Int = 0,
    @SerializedName("count")
    val count: Int,
    @SerializedName("totalCount")
    val totalCount: Int = 0,
    @SerializedName("systemTime")
    val systemTime: String? = null
)