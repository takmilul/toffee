package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class VastTagV3(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("adGroup")
    val adGroup: String,
    @SerializedName("url")
    val tags: List<String>?,
    @SerializedName("frequency")
    val frequency: Int? = 0,
)