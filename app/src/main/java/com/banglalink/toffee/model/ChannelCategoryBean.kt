package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class ChannelCategoryBean(
    @SerializedName("categories")
    val channelCategoryList: List<ChannelCategory>
)