package com.banglalink.toffee.model

import com.banglalink.toffee.enums.PageType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditorsChoiceFeaturedRequestParams (
    @SerialName("type")
    val type: String? = null,
    @SerialName("pageType")
    val pageType: PageType? = PageType.Landing,
    @SerialName("categoryId")
    val categoryId: Int = 0
)