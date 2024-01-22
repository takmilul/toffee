package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelDeleteVideoBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelVideoDeleteResponse(
    @SerialName("response")
    val response: MyChannelDeleteVideoBean
) : BaseResponse()