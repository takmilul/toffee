package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelEditBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelEditResponse(
    @SerialName("response")
    val response: MyChannelEditBean? = null
) : BaseResponse()