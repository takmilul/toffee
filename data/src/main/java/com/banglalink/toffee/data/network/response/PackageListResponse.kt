package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.PackageBean
import com.google.gson.annotations.SerializedName

data class PackageListResponse(
    @SerializedName("response")
    val response: PackageBean
) : BaseResponse()