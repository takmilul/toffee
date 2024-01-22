package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.FavoriteBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteResponse(
    @SerialName("response")
    val response: FavoriteBean
) : BaseResponse()