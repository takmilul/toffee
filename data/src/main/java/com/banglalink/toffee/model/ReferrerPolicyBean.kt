package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReferrerPolicyBean(
    @SerialName("isPromotionMessageEnabled")
    val isPromotionMessageEnabled: Boolean = false,
    @SerialName("promotionMessage")
    val promotionMessage: String? = null,
    @SerialName("messageReadMoreEnabled")
    val messageReadMoreEnabled: Boolean = false,
    @SerialName("readMoreDetails")
    val readMoreDetails: String? = null,
    @SerialName("fontSize")
    val fontSize: Int = 0,
    @SerialName("fontColor")
    val fontColor: String? = null
)