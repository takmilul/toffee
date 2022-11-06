package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class VastTagV2(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("description")
    var description: String = "",
    @SerializedName("adPosition")
    val adPosition: String?,
    @SerializedName("url")
    var url: String = "",
)
