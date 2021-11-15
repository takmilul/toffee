package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class ContentUploadResponse(
    @SerializedName("response")
    val response: ContentUploadResponseBean
) : BaseResponse()