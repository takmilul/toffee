package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseBean(
    @SerialName("systemTime")
    val systemTime: String,
    @SerialName("message")
    val message: String,
    @SerialName("messageType")
    val messageType: String
)