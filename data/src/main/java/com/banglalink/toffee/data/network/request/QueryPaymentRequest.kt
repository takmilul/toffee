package com.banglalink.toffee.data.network.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QueryPaymentRequest(
    @SerialName("paymentID")
    var paymentID: String? = null,
)