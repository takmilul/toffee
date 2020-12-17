package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class PartnersBean (
    @SerializedName(value = "channels", alternate = ["channelInfo"])
    val channels: List<ChannelInfo>?,
    val count: Int,
    val totalCount: Int = 0,
    val systemTime: String?=null
)