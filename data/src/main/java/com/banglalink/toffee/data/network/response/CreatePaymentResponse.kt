package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class CreatePaymentResponse (
	@SerializedName("statusMessage")
	val statusMessage : String? = null,
	@SerializedName("paymentID")
	val paymentId : String? = null,
	@SerializedName("bkashURL")
	val bKashUrl : String? = null,
	@SerializedName("callbackURL")
	val callbackURL : String? = null,
	@SerializedName("successCallbackURL")
	val successCallbackURL : String? = null,
	@SerializedName("failureCallbackURL")
	val failureCallbackURL : String? = null,
	@SerializedName("cancelledCallbackURL")
	val cancelledCallbackURL : String? = null,
	@SerializedName("amount")
	val amount : String? = null,
	@SerializedName("intent")
	val intent : String? = null,
	@SerializedName("currency")
	val currency : String? = null,
	@SerializedName("paymentCreateTime")
	val paymentCreateTime : String? = null,
	@SerializedName("transactionStatus")
	val transactionStatus : String? = null,
	@SerializedName("merchantInvoiceNumber")
	val merchantInvoiceNumber : String? = null,
	@SerializedName("message")
	val message : String? = null
): ExternalBaseResponse()