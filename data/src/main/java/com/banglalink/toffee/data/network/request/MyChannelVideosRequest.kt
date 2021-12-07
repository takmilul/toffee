package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class MyChannelVideosRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("limit")
    val limit: Int = 10
) : BaseRequest("getUgcChannelAllContent")