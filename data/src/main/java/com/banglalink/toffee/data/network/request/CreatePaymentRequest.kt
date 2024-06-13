package com.banglalink.toffee.data.network.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentRequest(
    @SerialName("mode")
    var mode: String? = null,
    @SerialName("payerReference")
    var payerReference: String? = null,
    @SerialName("callbackURL")
    var callbackURL: String? = null,
    @SerialName("merchantAssociationInfo")
    var merchantAssociationInfo: String? = null,
    @SerialName("amount")
    var amount: String? = null,
    @SerialName("currency")
    var currency: String? = null,
    @SerialName("intent")
    var intent: String? = null,
    @SerialName("merchantInvoiceNumber")
    var merchantInvoiceNumber: String? = null,
)