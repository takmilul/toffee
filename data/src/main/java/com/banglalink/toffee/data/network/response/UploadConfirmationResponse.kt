package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadConfirmationResponse(
    @SerialName("response")
    val response: ResponseBean? = null
) : BaseResponse()