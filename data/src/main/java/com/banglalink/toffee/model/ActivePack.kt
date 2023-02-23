package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

//@Entity
data class ActivePack(
    @SerializedName("pack_id")
    val packId: Int = 0,
    @SerializedName("contents")
    val contents: List<Int?>? = null,
    @SerializedName("is_subscription_active")
    val isActive: Boolean = false,
    @SerializedName("subscription_expiry")
    val expiryDate: String? = null,
    @SerializedName("subscription_pack_details")
    val packDetails: String? = null
)
