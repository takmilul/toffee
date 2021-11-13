package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MovieCategoryDetailBean (
    @SerializedName("count")
    val count: Int = 0,
    @SerializedName("totalCount")
    val totalCount: Int = 0,
    @SerializedName("cards")
    val cards: MoviesContentVisibilityCards? = null,
    @SerializedName("subCategoryWiseContent")
    val subCategoryWiseContent: List<MoviesSubCategoryWiseContent>?,
    @SerializedName("systemTime")
    val systemTime: String? = null,
)