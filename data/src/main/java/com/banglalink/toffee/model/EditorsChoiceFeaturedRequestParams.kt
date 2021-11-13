package com.banglalink.toffee.model

import com.banglalink.toffee.enums.PageType
import com.google.gson.annotations.SerializedName

data class EditorsChoiceFeaturedRequestParams (
    @SerializedName("type")
    val type: String,
    @SerializedName("pageType")
    val pageType: PageType,
    @SerializedName("categoryId")
    val categoryId: Int
)