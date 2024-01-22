package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoviesSubCategoryWiseContent(
    @SerialName("order_id")
    val orderId: Int = 0,
    @SerialName("sub_category_name")
    val subCategoryName: String? = null,
    @SerialName(value = "channels"/*, alternate = ["channelInfo"]*/)
    val channels: List<ChannelInfo>?,
)