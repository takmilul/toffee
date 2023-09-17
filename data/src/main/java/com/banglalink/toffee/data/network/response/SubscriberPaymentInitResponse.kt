package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class SubscriberPaymentInitResponse(
    @SerializedName("response")
    val response: SubscriberPaymentInitBean?
) : BaseResponse()

data class SubscriberPaymentInitBean(
    @SerializedName("status_code")
    val statusCode: Int? = null,
    @SerializedName("transaction_identifier_id")
    val transactionIdentifierId: String? = null,
    @SerializedName("web_view_url")
    val webViewUrl: String? = null,
    @SerializedName("callback_url")
    val callbackUrl: String? = null,
    @SerializedName("success_callback_url")
    val successCallbackUrl: String? = null,
    @SerializedName("failure_callback_url")
    val failureCallbackUrl: String? = null,
    @SerializedName("cancelled_callback_url")
    val cancelledCallbackUrl: String? = null,
    @SerializedName("message") val message: String? = null
)