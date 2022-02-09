package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MyChannelNavParams(
    @SerializedName("channelOwnerId")
    val channelOwnerId: Int,
    @SerializedName("pageTitle")
    val pageTitle: String = ""
)