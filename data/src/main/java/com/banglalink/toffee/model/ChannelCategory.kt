package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class ChannelCategory(
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("channels") 
    val channels: List<ChannelInfo>? = null
)