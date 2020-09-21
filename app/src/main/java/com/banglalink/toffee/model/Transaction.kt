package com.banglalink.toffee.model

data class Transaction(
    val type: String,
    val date: String,
    val amount: String,
    val remainingAmount: String
) 