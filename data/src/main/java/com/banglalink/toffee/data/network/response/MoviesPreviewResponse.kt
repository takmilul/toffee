package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MoviesPreviewBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoviesPreviewResponse(
    @SerialName("response")
    val response: MoviesPreviewBean
) : BaseResponse()