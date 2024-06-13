package com.banglalink.toffee.data.network.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChannelRequestParams(
    @SerialName("category")
    val category: String,
    @SerialName("categoryId")
    val categoryId: Int,
    @SerialName("subcategory")
    val subcategory: String,
    @SerialName("subcategoryId")
    val subcategoryId: Int,
    @SerialName("type")
    val type: String,
    @SerialName("isFilter")
    val isFilter: Int = 0,
    @SerialName("hashTag")
    val hashTag: String = "null"
)