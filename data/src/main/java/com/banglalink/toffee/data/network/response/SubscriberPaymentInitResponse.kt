package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriberPaymentInitResponse(
    @SerialName("response")
    val response: SubscriberPaymentInitBean? = null
) : BaseResponse()

@Serializable
data class SubscriberPaymentInitBean(
    @SerialName("status_code")
    val statusCode: Int? = null,
    @SerialName("transaction_identifier_id")
    val transactionIdentifierId: String? = null,
    @SerialName("web_view_url")
    val webViewUrl: String? = null,
    @SerialName("callback_url")
    val callbackUrl: String? = null,
    @SerialName("success_callback_url")
    val successCallbackUrl: String? = null,
    @SerialName("failure_callback_url")
    val failureCallbackUrl: String? = null,
    @SerialName("cancelled_callback_url")
    val cancelledCallbackUrl: String? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("response_from_where") val responseFromWhere: Int? = 0
)