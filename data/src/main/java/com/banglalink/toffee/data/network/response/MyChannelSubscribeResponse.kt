package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelSubscribeBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelSubscribeResponse(
    @SerialName("response")
    val response: MyChannelSubscribeBean
) : BaseResponse()