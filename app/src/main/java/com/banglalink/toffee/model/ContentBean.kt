package com.banglalink.toffee.model

import com.banglalink.toffee.ui.player.ChannelInfo
import com.google.gson.annotations.SerializedName

data class ContentBean(
    @SerializedName(value = "channels", alternate = ["channelInfo"])
    val channels: List<ChannelInfo>?,
    val count: Int,
    val totalCount: Int,
    val balance: Int,
    val systemTime: String
)