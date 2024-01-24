package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DramaSeriesContentBean (
    @SerialName(value = "channels"/*, alternate = ["channelInfo"]*/)
    val channels: List<ChannelInfo>? = null,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("totalCount")
    val totalCount: Int = 0,
    @SerialName("systemTime")
    val systemTime: String? = null
)