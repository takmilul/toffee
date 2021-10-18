package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class VastTag (
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("description")
    var description: String = "",

    @SerializedName("url")
    var url: String = "",
)