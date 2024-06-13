package com.banglalink.toffee.data.network.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExecutePaymentRequest(
    @SerialName("paymentID")
    var paymentId: String? = null,
)