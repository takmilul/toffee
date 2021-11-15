package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MoviesPreviewBean
import com.google.gson.annotations.SerializedName

data class MoviesPreviewResponse(
    @SerializedName("response")
    val response: MoviesPreviewBean
) : BaseResponse()