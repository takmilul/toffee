package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelDeleteVideoBean(
    @SerialName("message")
    val message: String? = null,
    @SerialName("messageType")
    val messageType: String? = null,
    @SerialName("systemTime")
    val systemTime: String? = null
)