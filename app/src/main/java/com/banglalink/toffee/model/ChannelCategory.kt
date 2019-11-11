package com.banglalink.toffee.model

import com.banglalink.toffee.ui.player.ChannelInfo
import com.google.gson.annotations.SerializedName

data class ChannelCategory(

    @SerializedName("category_name")
    val categoryName: String,

    val channels: List<ChannelInfo>? = null
)