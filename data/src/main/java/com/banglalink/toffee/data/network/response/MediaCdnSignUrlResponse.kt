package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaCdnSignUrlResponse(
    @SerialName("response")
    val response: MediaCdnSignUrl?
) : BaseResponse()