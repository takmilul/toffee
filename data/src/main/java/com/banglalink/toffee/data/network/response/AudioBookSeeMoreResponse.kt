package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AudioBookSeeMoreResponse(
    @SerialName("data" ) var data : List<CategoryData> = listOf()
): ExternalBaseResponse()

@Serializable
data class CategoryData (
    @SerialName("name" ) var name : String?         = null,
    @SerialName("data" ) var data : List<KabbikItemBean> = listOf()
)