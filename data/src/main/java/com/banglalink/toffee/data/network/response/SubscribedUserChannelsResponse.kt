package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.TrendingChannelBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscribedUserChannelsResponse(
    @SerialName("response")
    val response: TrendingChannelBean? = null
) : BaseResponse()