package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieCategoryDetailBean (
    @SerialName("count")
    val count: Int = 0,
    @SerialName("totalCount")
    val totalCount: Int = 0,
    @SerialName("cards")
    val cards: MoviesContentVisibilityCards? = null,
    @SerialName("subCategoryWiseContent")
    val subCategoryWiseContent: List<MoviesSubCategoryWiseContent>?,
    @SerialName("systemTime")
    val systemTime: String? = null,
)