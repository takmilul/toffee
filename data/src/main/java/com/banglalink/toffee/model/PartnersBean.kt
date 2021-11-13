package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class PartnersBean(
    @SerializedName(value = "channels", alternate = ["channelInfo"])
    val channels: List<ChannelInfo>?,
    @SerializedName("count")
    val count: Int,
    @SerializedName("totalCount")
    val totalCount: Int = 0,
    @SerializedName("systemTime")
    val systemTime: String? = null
)