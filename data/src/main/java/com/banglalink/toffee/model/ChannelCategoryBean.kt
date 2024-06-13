package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChannelCategoryBean(
    @SerialName("categories")
    val channelCategoryList: List<ChannelCategory>
)