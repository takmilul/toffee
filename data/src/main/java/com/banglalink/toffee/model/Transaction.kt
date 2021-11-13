package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class Transaction(
    @SerializedName("type")
    val type: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("amount")
    val amount: String,
    @SerializedName("remainingAmount")
    val remainingAmount: String
)