package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class ContentEditResponse(
    @SerializedName("response")
    val response: ResponseBean
) : BaseResponse()