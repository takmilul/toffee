package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//@Entity
@Serializable
data class ActivePack(
    @SerialName("packId")
    val packId: Int = 0,
    @SerialName("contents")
    val contents: List<Int>? = null,
    @SerialName("isSubscriptionActive")
    var isActive: Boolean = false,
    @SerialName("expiryDate")
    val expiryDate: String? = null,
    @SerialName("dataPackDetails")
    val packDetail: String? = null,
    @SerialName("isFreeUsed")
    val isTrialPackUsed: Boolean = false
)