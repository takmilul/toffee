package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MyChannelVideosBean (
    @SerializedName(value = "channels", alternate = ["channelInfo"])
    val channels: List<ChannelInfo>?,
    val count: Int,
    val isOwner: Int,
    val totalCount: Int = 0,
    val systemTime: String?=null
)