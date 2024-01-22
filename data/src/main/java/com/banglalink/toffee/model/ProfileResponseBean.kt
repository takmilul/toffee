package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponseBean(
    @SerialName("code")
    val code: Int,
    @SerialName("message")
    val message: String?
)