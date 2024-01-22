package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComingSoonContent(
    @SerialName("id")
    val id: Long,
    @SerialName("program_name")
    val program_name: String?,
    @SerialName("url_type")
    val url_type: Int,
    @SerialName("coming_soon_poster_url")
    val coming_soon_poster_url: String?,
)