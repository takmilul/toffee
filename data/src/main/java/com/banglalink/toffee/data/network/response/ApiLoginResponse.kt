package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.CustomerInfoLogin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiLoginResponse(
    @SerialName("response")
    val customerInfoLogin: CustomerInfoLogin?
) : BaseResponse()