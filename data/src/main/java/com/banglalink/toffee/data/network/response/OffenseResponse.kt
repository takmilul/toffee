package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.OffenseBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OffenseResponse(
    @SerialName("response")
    val response: OffenseBean
) : BaseResponse()