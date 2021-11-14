package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class ChannelRequestParams(
    @SerializedName("category")
    val category: String,
    @SerializedName("categoryId")
    val categoryId: Int,
    @SerializedName("subcategory")
    val subcategory: String,
    @SerializedName("subcategoryId")
    val subcategoryId: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("isFilter")
    val isFilter: Int = 0,
    @SerializedName("hashTag")
    val hashTag: String = "null"
)