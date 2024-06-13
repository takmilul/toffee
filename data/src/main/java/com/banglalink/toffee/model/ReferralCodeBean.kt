package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReferralCodeBean(
    @SerialName("referralCode")
    val referralCode: String? = null,
    @SerialName("sharableText")
    val sharableText: String? = null
)