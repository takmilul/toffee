package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FireworkResponse(
    @SerialName("response")
    val response: FireworkBean? = null
):BaseResponse()
