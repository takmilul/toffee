package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VastTagV3(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("adGroup")
    val adGroup: String,
    @SerialName("url")
    val tags: List<String>?,
)