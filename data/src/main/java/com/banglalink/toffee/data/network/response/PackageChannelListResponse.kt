package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.PackageDetailsBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackageChannelListResponse(
    @SerialName("response")
    val response: PackageDetailsBean
) : BaseResponse()