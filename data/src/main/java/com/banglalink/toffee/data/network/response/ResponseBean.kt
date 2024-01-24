package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseBean(
    @SerialName("systemTime")
    val systemTime: String? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("messageType")
    val messageType: String? = null
)