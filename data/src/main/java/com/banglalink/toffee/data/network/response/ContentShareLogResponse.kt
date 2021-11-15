package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ContentShareLogBean
import com.google.gson.annotations.SerializedName

data class ContentShareLogResponse(
    @SerializedName("response")
    val response: ContentShareLogBean
) : BaseResponse()