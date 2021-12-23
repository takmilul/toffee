package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class MyChannelRatingRequest(
    @SerializedName("channelId")
    val channelId: Int,
    @SerializedName("rating")
    val rating: Float,
    @SerializedName("channelOwnerId")
    val channelOwnerId: Int,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String
) : BaseRequest(ApiNames.RATE_CHANNEL)
