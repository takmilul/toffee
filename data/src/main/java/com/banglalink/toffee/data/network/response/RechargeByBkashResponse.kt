package com.banglalink.toffee.data.network.response

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RechargeByBkashResponse(
    @SerialName("response")
    val response: RechargeByBkashBean? = null
) : BaseResponse()

@Serializable
data class RechargeByBkashBean(
    @SerialName("statusCode")
    val statusCode: Int = 0,
    @SerialName("status")
    val status: String? = null,
    @Contextual
    @SerialName("message")
    val message: Any? = null,
    @SerialName("data")
    val data: RechargeByBkashData? = null
)

@Serializable
data class RechargeByBkashData(
    @SerialName("tran_id")
    val transactionId: String? = null,
    @SerialName("total_payment_amount")
    val totalPaymentAmount: Int? = 0,
    @SerialName("currency")
    val currency: String? = null,
    @SerialName("payment_create_time")
    val paymentCreateTime: String? = null,
    @SerialName("webview_url")
    val bKashWebUrl: String? = null,
    @SerialName("success_callback_url")
    val successCallbackUrl: String? = null,
    @SerialName("cancel_callback_url")
    val cancelCallbackUrl: String? = null,
    @SerialName("failure_callback_url")
    val failureCallbackUrl: String? = null
)