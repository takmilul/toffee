package com.banglalink.toffee.data.network.response

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CircuitBreakerData (
    @SerialName("isActive")
    val isActive: Boolean = false,
    @Contextual
    @SerialName("updatedAt")
    val updatedAt: Date? = null,
    @Contextual
    @SerialName("expiredAt")
    val expiredAt: Date? = null
)