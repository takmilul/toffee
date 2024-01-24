package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RedeemReferralCodeBean(
    @SerialName("referralStatus")
    val referralStatus: String? = null,
    @SerialName("referralStatusMessage")
    val referralStatusMessage: String? = null,
    @SerialName("isRedeemSuccess")
    val isRedeemSuccess: Boolean? = false,
    @SerialName("messageTitle")
    val title: String? = null,
    @SerialName("messageBody")
    val message: String? = null,
    @SerialName("isBulletPointMessage")
    val isBullterPointMessage: Boolean? = false,
    @SerialName("bulletMessage")
    val bulletMessage: List<String>? = null,
    @SerialName("messageType")
    val messageType: String? = null
)