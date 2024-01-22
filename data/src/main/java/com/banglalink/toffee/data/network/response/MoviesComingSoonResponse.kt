package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ComingSoonBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoviesComingSoonResponse(
    @SerialName("response")
    val response: ComingSoonBean
) : BaseResponse()