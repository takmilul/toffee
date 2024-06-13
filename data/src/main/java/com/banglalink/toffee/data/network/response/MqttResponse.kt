package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MqttResponse(
    @SerialName("response")
    val response: MqttBean? = null
) : BaseResponse()