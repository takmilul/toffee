package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class ProfileBean(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("balance")
    val balance: Int,
    @SerializedName("customer")
    val customer: Customer
)