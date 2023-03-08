package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class QueryPaymentResponse(
	@SerializedName("paymentID")
	val paymentID: String? = null,
	@SerializedName("mode")
	val mode: String? = null,
	@SerializedName("paymentCreateTime")
	val paymentCreateTime: String? = null,
	@SerializedName("amount")
	val amount: String? = null,
	@SerializedName("currency")
	val currency: String? = null,
	@SerializedName("intent")
	val intent: String? = null,
	@SerializedName("merchantInvoice")
	val merchantInvoice: String? = null,
	@SerializedName("transactionStatus")
	val transactionStatus: String? = null,
	@SerializedName("verificationStatus")
	val verificationStatus: String? = null,
	@SerializedName("statusCode")
	val statusCode: String? = null,
	@SerializedName("statusMessage")
	val statusMessage: String? = null,
	@SerializedName("payerReference")
	val payerReference: String? = null,
	@SerializedName("agreementID")
	val agreementID: String? = null,
	@SerializedName("agreementStatus")
	val agreementStatus: String? = null,
	@SerializedName("agreementCreateTime")
	val agreementCreateTime: String? = null,
	@SerializedName("agreementExecuteTime")
	val agreementExecuteTime: String? = null,
) : BaseResponse()