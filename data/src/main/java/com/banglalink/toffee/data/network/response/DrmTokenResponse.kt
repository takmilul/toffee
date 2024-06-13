package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.DrmToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DrmTokenResponse(
    @SerialName("response")
    val response: DrmToken? = null
) : BaseResponse()