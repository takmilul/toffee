package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelRatingBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelRatingResponse(
    @SerialName("response")
    val response: MyChannelRatingBean
) : BaseResponse()