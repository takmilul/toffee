package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DrmToken(
    @SerialName("drmToken")
    val drmToken: String? = null
)