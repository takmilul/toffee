package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class FavoriteBean(
    @SerializedName("isFavorite")
    val isFavorite: Int,
    @SerializedName("message")
    val message: String? = null
)