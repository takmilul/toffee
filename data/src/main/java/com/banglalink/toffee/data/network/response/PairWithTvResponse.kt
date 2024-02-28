package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PairWithTvResponse(
    @SerialName("response")
    val response: PairStatus
) : BaseResponse()

@Serializable
data class PairStatus(
    @SerialName("status")
    val status: Int // 0 = wrong code, 1 = active, 2 = expired
)