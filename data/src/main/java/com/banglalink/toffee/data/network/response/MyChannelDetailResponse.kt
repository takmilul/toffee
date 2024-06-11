package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelDetailBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class MyChannelDetailResponse(
    @SerialName("response")
    val response: MyChannelDetailBean? = null
) : BaseResponse()