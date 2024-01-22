package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentShareLogBean(
    @SerialName("code")
    val code: Int,
    @SerialName("message")
    val message: String? = null,
    @SerialName("messageType")
    val messageType: String? = null
)