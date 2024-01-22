package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class SubscribePackageResponse(
    @SerialName("response")
    val response: BodyResponse
) : BaseResponse()