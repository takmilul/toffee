package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VastTagV3(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("adGroup")
    val adGroup: String? = null,
    @SerialName("url")
    val tags: List<String>? = null,
    @SerialName("frequency")
    val frequency: Int? = 0,
)