package com.banglalink.toffee.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethodBean(
    @SerialName("payment")
    val paymentList: List<Payment>? = null
)