package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class ExecutePaymentResponse (
	@SerializedName("statusCode")
	val statusCode : String? = null,
	@SerializedName("statusMessage")
	val statusMessage : String? = null,
	@SerializedName("paymentID")
	val paymentID : String? = null,
	@SerializedName("payerReference")
	val payerReference : String? = null,
	@SerializedName("customerMsisdn")
	val customerMsisdn : String? = null,
	@SerializedName("trxID")
	val trxID : String? = null,
	@SerializedName("amount")
	val amount : String? = null,
	@SerializedName("transactionStatus")
	val transactionStatus : String? = null,
	@SerializedName("paymentExecuteTime")
	val paymentExecuteTime : String? = null,
	@SerializedName("currency")
	val currency : String? = null,
	@SerializedName("intent")
	val intent : String? = null,
	@SerializedName("merchantInvoiceNumber")
	val merchantInvoiceNumber : String? = null
) : BaseResponse()