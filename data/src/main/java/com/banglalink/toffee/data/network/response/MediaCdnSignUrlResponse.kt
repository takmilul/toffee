package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.DrmToken
import com.google.gson.annotations.SerializedName

data class MediaCdnSignUrlResponse(
    @SerializedName("response")
    val response: MediaCdnSignUrl?
) : BaseResponse()