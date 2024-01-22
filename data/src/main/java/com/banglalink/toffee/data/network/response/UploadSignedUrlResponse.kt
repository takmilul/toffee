package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.UploadSignedUrl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadSignedUrlResponse(
    @SerialName("response")
    val response: UploadSignedUrl
) : BaseResponse()