package com.banglalink.toffee.model

data class MovieCategoryDetailBean (
    val count: Int = 0,
    val totalCount: Int = 0,
    val cards: MoviesContentVisibilityCards? = null,
    val subCategoryWiseContent: List<MoviesSubCategoryWiseContent>?,
    val systemTime: String? = null,
)