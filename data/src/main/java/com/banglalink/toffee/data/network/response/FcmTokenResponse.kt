package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FcmTokenResponse(
    @SerialName("response")
    val response: FcmTokenBean? = null
) : BaseResponse()