package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.UploadSignedUrl
import com.google.gson.annotations.SerializedName

data class UploadSignedUrlResponse(
    @SerializedName("response")
    val response: UploadSignedUrl
) : BaseResponse()