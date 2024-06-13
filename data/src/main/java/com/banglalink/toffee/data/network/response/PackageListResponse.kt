package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.PackageBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackageListResponse(
    @SerialName("response")
    val response: PackageBean? = null
) : BaseResponse()