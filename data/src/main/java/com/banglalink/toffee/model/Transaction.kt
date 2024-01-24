package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    @SerialName("type")
    val type: String? = null,
    @SerialName("date")
    val date: String? = null,
    @SerialName("amount")
    val amount: String? = null,
    @SerialName("remainingAmount")
    val remainingAmount: String? = null
)