package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class TrendingChannelBean(
    @SerializedName("channels")
    val channels: List<TrendingChannelInfo>?,
    var code: Int = 0,
    val count: Int,
    val totalCount: Int = 0,
    val systemTime: String?=null
)