package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QueryPaymentResponse(
	@SerialName("paymentID")
	val paymentID: String? = null,
	@SerialName("trxID")
	var transactionId: String? = null,
	@SerialName("mode")
	val mode: String? = null,
	@SerialName("paymentCreateTime")
	val paymentCreateTime: String? = null,
	@SerialName("amount")
	val amount: String? = null,
	@SerialName("currency")
	val currency: String? = null,
	@SerialName("intent")
	val intent: String? = null,
	@SerialName("merchantInvoice")
	val merchantInvoice: String? = null,
	@SerialName("transactionStatus")
	val transactionStatus: String? = null,
	@SerialName("verificationStatus")
	val verificationStatus: String? = null,
	@SerialName("statusMessage")
	val statusMessage: String? = null,
	@SerialName("payerReference")
	val payerReference: String? = null,
	@SerialName("customerMsisdn")
	var customerMsisdn: String? = null,
	@SerialName("agreementID")
	val agreementID: String? = null,
	@SerialName("agreementStatus")
	val agreementStatus: String? = null,
	@SerialName("agreementCreateTime")
	val agreementCreateTime: String? = null,
	@SerialName("agreementExecuteTime")
	val agreementExecuteTime: String? = null,
) : ExternalBaseResponse()