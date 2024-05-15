package com.banglalink.toffee.data.network.response

import kotlinx.serialization.Serializable

@Serializable
data class DrmLicenseResponse(
    val success: Boolean? = null,
    val data: Data? = null,
    val message: String? = null
): BaseResponse()

@Serializable
data class Data(
    val payload: String? = null
)