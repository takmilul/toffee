package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.PackageDetailsBean
import com.google.gson.annotations.SerializedName

data class PackageChannelListResponse(
    @SerializedName("response")
    val response: PackageDetailsBean
) : BaseResponse()