package com.banglalink.toffee.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class MyChannelVideosBean (
    @JsonNames("channelInfo")
    val channels: List<ChannelInfo>? = null,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("isOwner")
    val isOwner: Int = 0,
    @SerialName("totalCount")
    val totalCount: Int = 0,
    @SerialName("systemTime")
    val systemTime: String? = null
)