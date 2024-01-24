package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileBean(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("balance")
    val balance: Int = 0,
    @SerialName("customer")
    val customer: Customer
)