package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerSessionDetails(
    @SerialName("durationInSec")
    val durationInSec: Long,
    @SerialName("bandWidthInMB")
    val bandWidthInMB: Double
)