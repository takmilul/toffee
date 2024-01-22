package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReferrerPolicyBean(
    @SerialName("isPromotionMessageEnabled")
    val isPromotionMessageEnabled: Boolean,
    @SerialName("promotionMessage")
    val promotionMessage: String?,
    @SerialName("messageReadMoreEnabled")
    val messageReadMoreEnabled: Boolean,
    @SerialName("readMoreDetails")
    val readMoreDetails: String?,
    @SerialName("fontSize")
    val fontSize: Int,
    @SerialName("fontColor")
    val fontColor: String
)