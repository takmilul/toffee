package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HeartBeatResponse(
    @SerialName("response")
    val response: HeartBeatBean? = null
) : BaseResponse()