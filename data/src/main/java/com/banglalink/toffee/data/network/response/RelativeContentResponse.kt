package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ContentBean
import com.google.gson.annotations.SerializedName

data class RelativeContentResponse(
    @SerializedName("response")
    val response: ContentBean
) : BaseResponse()