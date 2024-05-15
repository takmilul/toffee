package com.banglalink.toffee.data.network.interceptor


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DrmLicenseResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("data")
    val `data`: Data? = null,
    @SerialName("message")
    val message: String? = null
)

@Serializable
data class Data(
    @SerialName("payload")
    val payload: String? = null
)