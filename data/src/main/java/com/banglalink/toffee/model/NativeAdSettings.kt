package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NativeAdSettings(
    @SerialName("area")
    val area: Int = 0,
    @SerialName("isActive")
    val isActive: Boolean = false,
    @SerialName("adUnitId")
    val adUnitId: String? = null,
    @SerialName("adInterval")
    val adInterval: Int = 4,
)