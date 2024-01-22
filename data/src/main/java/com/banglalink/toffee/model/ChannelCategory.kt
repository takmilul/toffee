package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChannelCategory(
    @SerialName("category_name")
    val categoryName: String,
    @SerialName("channels") 
    val channels: List<ChannelInfo>? = null
)