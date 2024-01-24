package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogoutBean(
    @SerialName("customerId")
    val customerId: Int = 0,
    @SerialName("password")
    val password: String? = null,
    @SerialName("verified_status")
    val verifyStatus: Boolean = false
)