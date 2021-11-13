package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class PlayerSessionDetails(
    @SerializedName("durationInSec")
    val durationInSec: Long,
    @SerializedName("bandWidthInMB")
    val bandWidthInMB: Double
)