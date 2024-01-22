package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ChannelCategoryBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AllChannelResponse(
    @SerialName("response")
    val response: ChannelCategoryBean
) : BaseResponse()