package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoviesPreviewBean (
    @SerialName(value = "channels"/*, alternate = ["channelInfo"]*/)
    val channels: List<ChannelInfo>?,
    @SerialName("count")
    val count: Int,
    @SerialName("totalCount")
    val totalCount: Int = 0,
    @SerialName("systemTime")
    val systemTime: String?=null
)