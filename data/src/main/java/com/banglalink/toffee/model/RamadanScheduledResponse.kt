package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class RamadanScheduledResponse(
    @SerializedName("ramadanScheduled")
    val ramadanSchedule: List<RamadanSchedule>? = null,
    @SerializedName("serverDateTime")
    val serverDateTime: String? = null
)