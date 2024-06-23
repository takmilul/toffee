package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelVideosBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelVideosResponse(
    @SerialName("response")
    val response: MyChannelVideosBean? = null
) : BaseResponse()