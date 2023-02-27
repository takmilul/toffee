package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

//@Entity
data class ActivePack(
    @SerializedName("packId")
    val packId: Int = 0,
    @SerializedName("contents")
    val contents: List<Int>? = null,
    @SerializedName("isSubscriptionActive")
    var isActive: Boolean = false,
    @SerializedName("expiryDate")
    val expiryDate: String? = null,
    @SerializedName("dataPackDetails")
    val packDetail: String? = null,
    @SerializedName("isFreeUsed")
    val isTrialPackUsed: Boolean = false
)