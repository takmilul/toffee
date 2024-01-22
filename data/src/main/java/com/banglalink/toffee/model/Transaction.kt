package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    @SerialName("type")
    val type: String,
    @SerialName("date")
    val date: String,
    @SerialName("amount")
    val amount: String,
    @SerialName("remainingAmount")
    val remainingAmount: String
)