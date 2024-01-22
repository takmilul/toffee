package com.banglalink.toffee.data.network.response

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class CircuitBreakerData (
    @SerialName("isActive")
    val isActive: Boolean,
    @Contextual
    @SerialName("updatedAt")
    val updatedAt: Date,
    @Contextual
    @SerialName("expiredAt")
    val expiredAt: Date
)