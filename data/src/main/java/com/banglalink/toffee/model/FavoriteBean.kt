package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteBean(
    @SerialName("isFavorite")
    val isFavorite: Int = 0,
    @SerialName("message")
    val message: String? = null
)