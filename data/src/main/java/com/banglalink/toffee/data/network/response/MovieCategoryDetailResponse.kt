package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MovieCategoryDetailBean
import com.google.gson.annotations.SerializedName

data class MovieCategoryDetailResponse(
    @SerializedName("response")
    val response: MovieCategoryDetailBean
) : BaseResponse()