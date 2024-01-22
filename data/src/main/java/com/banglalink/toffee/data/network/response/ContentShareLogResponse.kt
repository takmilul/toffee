package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ContentShareLogBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentShareLogResponse(
    @SerialName("response")
    val response: ContentShareLogBean
) : BaseResponse()