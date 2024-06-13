package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RamadanScheduledResponse(
    @SerialName("ramadanScheduled")
    val ramadanSchedule: List<RamadanSchedule>? = null,
    @SerialName("serverDateTime")
    val serverDateTime: String? = null
)