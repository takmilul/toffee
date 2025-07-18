package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ContentBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteContentResponse(
    @SerialName("response")
    val response: ContentBean? = null
) : BaseResponse()