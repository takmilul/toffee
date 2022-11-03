package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

class VastTagBean(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("adGroup")
    val adGroup: String,
    @SerializedName("url")
    val vodTags: List<String>?,
)