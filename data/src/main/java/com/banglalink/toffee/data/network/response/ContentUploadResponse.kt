package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentUploadResponse(
    @SerialName("response")
    val response: ContentUploadResponseBean
) : BaseResponse()