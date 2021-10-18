package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MoviesSubCategoryWiseContent (
    @SerializedName("order_id")
    val orderId: Int = 0,
    @SerializedName("sub_category_name")
    val subCategoryName: String? = null,
    @SerializedName(value = "channels", alternate = ["channelInfo"])
    val channels: List<ChannelInfo>?,
)