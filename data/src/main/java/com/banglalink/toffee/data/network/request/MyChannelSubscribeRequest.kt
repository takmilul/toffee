package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelSubscribeRequest(
    @SerialName("channelId")
    val channelId: Int,
    @SerialName("isSubscribed")
    val isSubscribed: Int,
    @SerialName("channelOwnerId")
    val channelOwnerId: Int,
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String
) : BaseRequest(ApiNames.SUBSCRIBE_CHANNEL)