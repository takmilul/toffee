package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KeepAliveResponse(
    @SerialName("response")
    val response: KeepAliveBean? = null
) : BaseResponse()

@Serializable
data class KeepAliveBean(
    @SerialName("code")
    val code: Int?,
    @SerialName("message")
    val message: String?

)