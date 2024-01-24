package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserChannelBean(
    @SerialName("channels")
    val channels: List<UserChannelInfo>? = null,
    @SerialName("subscriberCount")
    var subscriberCount: Int = 0,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("totalCount")
    val totalCount: Int = 0,
    @SerialName("systemTime")
    val systemTime: String? = null
)