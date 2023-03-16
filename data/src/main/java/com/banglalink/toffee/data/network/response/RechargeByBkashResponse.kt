package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class RechargeByBkashResponse(
    @SerializedName("response")
    val response: RechargeByBkashBean?
) : BaseResponse()

data class RechargeByBkashBean(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: Any? = null,
    @SerializedName("data")
    val data: RechargeByBkashData? = null
)

data class RechargeByBkashData(
    @SerializedName("tran_id")
    val transactionId: String? = null,
    @SerializedName("total_payment_amount")
    val totalPaymentAmount: Int? = 0,
    @SerializedName("currency")
    val currency: String? = null,
    @SerializedName("payment_create_time")
    val paymentCreateTime: String? = null,
    @SerializedName("webview_url")
    val bKashWebUrl: String? = null,
    @SerializedName("success_callback_url")
    val successCallbackUrl: String? = null,
    @SerializedName("cancel_callback_url")
    val cancelCallbackUrl: String? = null,
    @SerializedName("failure_callback_url")
    val failureCallbackUrl: String? = null
)