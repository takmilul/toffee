package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.LogoutBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogoutResponse(
    @SerialName("response")
    val response: LogoutBean
) : BaseResponse()