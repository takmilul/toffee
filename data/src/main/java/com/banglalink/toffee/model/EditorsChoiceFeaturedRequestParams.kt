package com.banglalink.toffee.model

import com.banglalink.toffee.enums.PageType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditorsChoiceFeaturedRequestParams (
    @SerialName("type")
    val type: String,
    @SerialName("pageType")
    val pageType: PageType,
    @SerialName("categoryId")
    val categoryId: Int
)