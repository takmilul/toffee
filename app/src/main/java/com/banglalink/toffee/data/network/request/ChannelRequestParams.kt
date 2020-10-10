package com.banglalink.toffee.data.network.request

data class ChannelRequestParams(
    val category: String,
    val categoryId: Int,
    val subcategory: String,
    val subcategoryId: Int,
    val type: String
)