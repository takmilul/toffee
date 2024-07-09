package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
data class DrmTokenV1Response(
    @SerialName("response")
    val response: DrmTokenV1?
) : BaseResponse()