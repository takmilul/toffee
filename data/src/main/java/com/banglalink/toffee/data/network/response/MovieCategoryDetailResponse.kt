package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MovieCategoryDetailBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieCategoryDetailResponse(
    @SerialName("response")
    val response: MovieCategoryDetailBean
) : BaseResponse()