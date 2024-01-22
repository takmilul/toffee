package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DrmTokenRequest(
    @SerialName("contentID")
    val contentID: String,
    @SerialName("userID")
    val userID: String,
    @SerialName("password")
    val password: String,
    @SerialName("drmType")
    val drmType: String = "Widevine",
    @SerialName("licenseDuration")
    val licenseDuration: Int = 0
) : BaseRequest(ApiNames.GET_DRM_TOKEN)