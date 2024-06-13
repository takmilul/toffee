package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelVideosRequest(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("offset")
    val offset: Int,
    @SerialName("limit")
    val limit: Int = 10
) : BaseRequest(ApiNames.GET_MY_CHANNEL_ALL_VIDEOS)