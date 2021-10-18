package com.banglalink.toffee.model

import com.banglalink.toffee.enums.PageType

data class EditorsChoiceFeaturedRequestParams (
    val type: String,
    val pageType: PageType,
    val categoryId: Int
)