package com.banglalink.toffee.model
import com.google.gson.annotations.SerializedName

data class PaymentMethodBean(
    @SerializedName("payment")
    val paymentList: List<Payment>?
)