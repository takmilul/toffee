package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class DrmTokenV1Request (
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String
) : BaseRequest(ApiNames.GET_DRM_TOKEN_V1)