package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class ContentBean(
    @SerializedName(value = "channels", alternate = ["channelInfo"])
    val channels: List<ChannelInfo>?,
    val count: Int,
    val totalCount: Int,
    val balance: Int=0,
    val systemTime: String?=null
)