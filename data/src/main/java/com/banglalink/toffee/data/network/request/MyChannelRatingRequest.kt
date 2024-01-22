package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelRatingRequest(
    @SerialName("channelId")
    val channelId: Int,
    @SerialName("rating")
    val rating: Float,
    @SerialName("channelOwnerId")
    val channelOwnerId: Int,
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String
) : BaseRequest(ApiNames.RATE_CHANNEL)
