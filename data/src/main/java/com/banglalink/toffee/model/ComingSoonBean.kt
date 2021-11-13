package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class ComingSoonBean (
    @SerializedName(value = "channels", alternate = ["channelInfo"])
    val channels: List<ComingSoonContent>?,
    @SerializedName("count")
    val count: Int,
    @SerializedName("totalCount")
    val totalCount: Int = 0,
    @SerializedName("systemTime")
    val systemTime: String?=null
)