package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteBean(
    @SerialName("isFavorite")
    val isFavorite: Int,
    @SerialName("message")
    val message: String? = null
)