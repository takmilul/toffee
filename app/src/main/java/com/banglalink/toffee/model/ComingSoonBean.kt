package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class ComingSoonBean (
    @SerializedName(value = "channels", alternate = ["channelInfo"])
    val channels: List<ComingSoonContent>?,
    val count: Int,
    val totalCount: Int = 0,
    val systemTime: String?=null
)