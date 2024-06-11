package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.RamadanScheduledResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BubbleResponse(
    @SerialName("response")
    val response: RamadanScheduledResponse? = null,
) : BaseResponse()
