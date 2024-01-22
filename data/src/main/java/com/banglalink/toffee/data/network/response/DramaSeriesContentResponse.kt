package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.DramaSeriesContentBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DramaSeriesContentResponse(
    @SerialName("response")
    val response: DramaSeriesContentBean
) : BaseResponse()