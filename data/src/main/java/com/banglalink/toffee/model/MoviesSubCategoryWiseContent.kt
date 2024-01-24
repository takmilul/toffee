package com.banglalink.toffee.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class MoviesSubCategoryWiseContent(
    @SerialName("order_id")
    val orderId: Int = 0,
    @SerialName("sub_category_name")
    val subCategoryName: String? = null,
    @JsonNames("channelInfo")
    val channels: List<ChannelInfo>?,
)