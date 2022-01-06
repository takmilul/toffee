package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class FireworkBean(
    @SerializedName(value = "fireworks")
    val fireworkModels: List<FireworkModel>?,
    @SerializedName("count")
    val count: Int,
    @SerializedName("totalCount")
    val totalCount: Int = 0,
    @SerializedName("systemTime")
    val systemTime: String? = null
)
