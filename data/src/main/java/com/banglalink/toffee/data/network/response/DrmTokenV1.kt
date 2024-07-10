package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DrmTokenV1(
    @SerialName("drmToken")
    val drmTokenV1: String
)