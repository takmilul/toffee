package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComingSoonContent(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("program_name")
    val program_name: String? = null,
    @SerialName("url_type")
    val url_type: Int = 0,
    @SerialName("coming_soon_poster_url")
    val coming_soon_poster_url: String? = null,
)