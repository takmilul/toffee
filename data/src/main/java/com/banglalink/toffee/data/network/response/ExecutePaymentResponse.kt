package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExecutePaymentResponse (
	@SerialName("statusMessage")
	val statusMessage : String? = null,
	@SerialName("paymentID")
	val paymentID : String? = null,
	@SerialName("payerReference")
	val payerReference : String? = null,
	@SerialName("customerMsisdn")
	val customerMsisdn : String? = null,
	@SerialName("trxID")
	val transactionId : String? = null,
	@SerialName("amount")
	val amount : String? = null,
	@SerialName("transactionStatus")
	val transactionStatus : String? = null,
	@SerialName("paymentExecuteTime")
	val paymentExecuteTime : String? = null,
	@SerialName("currency")
	val currency : String? = null,
	@SerialName("intent")
	val intent : String? = null,
	@SerialName("merchantInvoiceNumber")
	val merchantInvoiceNumber : String? = null
) : ExternalBaseResponse()