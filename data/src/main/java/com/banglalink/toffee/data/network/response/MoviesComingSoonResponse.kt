package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ComingSoonBean
import com.google.gson.annotations.SerializedName

data class MoviesComingSoonResponse(
    @SerializedName("response")
    val response: ComingSoonBean
) : BaseResponse()