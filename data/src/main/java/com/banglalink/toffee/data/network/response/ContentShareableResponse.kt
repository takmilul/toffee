package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ContentBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentShareableResponse(
    @SerialName("response")
    val response: ContentBean
) : BaseResponse()