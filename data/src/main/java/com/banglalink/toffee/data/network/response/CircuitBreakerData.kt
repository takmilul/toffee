package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName
import java.util.*

data class CircuitBreakerData (
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("updatedAt")
    val updatedAt: Date,
    @SerializedName("expiredAt")
    val expiredAt: Date
)