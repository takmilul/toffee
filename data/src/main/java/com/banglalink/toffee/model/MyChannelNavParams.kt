package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelNavParams(
    @SerialName("channelOwnerId")
    val channelOwnerId: Int = 0,
    @SerialName("pageTitle")
    val pageTitle: String = ""
)