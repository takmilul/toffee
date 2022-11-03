package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class VastTagResponse(
    @SerializedName("response")
    val response: List<VastTagBean>
) : BaseResponse()