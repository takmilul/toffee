package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.UserChannelBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PopularChannelsResponse(
    @SerialName("response")
    val response: UserChannelBean? = null
) : BaseResponse()