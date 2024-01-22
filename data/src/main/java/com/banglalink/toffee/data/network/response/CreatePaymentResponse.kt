package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentResponse (
	@SerialName("statusMessage")
	val statusMessage : String? = null,
	@SerialName("paymentID")
	val paymentId : String? = null,
	@SerialName("bkashURL")
	val bKashUrl : String? = null,
	@SerialName("callbackURL")
	val callbackURL : String? = null,
	@SerialName("successCallbackURL")
	val successCallbackURL : String? = null,
	@SerialName("failureCallbackURL")
	val failureCallbackURL : String? = null,
	@SerialName("cancelledCallbackURL")
	val cancelledCallbackURL : String? = null,
	@SerialName("amount")
	val amount : String? = null,
	@SerialName("intent")
	val intent : String? = null,
	@SerialName("currency")
	val currency : String? = null,
	@SerialName("paymentCreateTime")
	val paymentCreateTime : String? = null,
	@SerialName("transactionStatus")
	val transactionStatus : String? = null,
	@SerialName("merchantInvoiceNumber")
	val merchantInvoiceNumber : String? = null,
	@SerialName("message")
	val message : String? = null
): ExternalBaseResponse()