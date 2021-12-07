package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class ComingSoonContent(
    @SerializedName("id")
    val id: Long,
    @SerializedName("program_name")
    val program_name: String?,
    @SerializedName("url_type")
    val url_type: Int,
    @SerializedName("coming_soon_poster_url")
    val coming_soon_poster_url: String?,
)