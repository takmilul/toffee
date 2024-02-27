package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.PAYMENT_LOG_FROM_DEVICE
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendPaymentLogFromDeviceEvent @Inject constructor() {
    
    fun execute(paymentLogFromDeviceData: PaymentLogFromDeviceData) {
        PubSubMessageUtil.send(paymentLogFromDeviceData, PAYMENT_LOG_FROM_DEVICE)
    }
}

data class PaymentLogFromDeviceData(
    @SerializedName("id")
    val id : Long = 0,
    @SerializedName("callingApiName")
    val callingApiName : String? = null,
    @SerializedName("packId")
    val packId : Int = 0,
    @SerializedName("packTitle")
    val packTitle : String? = null,
    @SerializedName("dataPackId")
    val dataPackId : Int = 0,
    @SerializedName("dataPackDetails")
    val dataPackDetails : String? = null,
    @SerializedName("paymentMethodId")
    val paymentMethodId : Int = 0,
    @SerializedName("paymentMsisdn")
    val paymentMsisdn : String? = null,
    @SerializedName("paymentId")
    val paymentId : String? = null,
    @SerializedName("trxId")
    val transactionId : String? = null,
    @SerializedName("transactionStatus")
    val transactionStatus : String? = null,
    @SerializedName("amount")
    val amount : String? = null,
    @SerializedName("merchantInvoiceNumber")
    val merchantInvoiceNumber : String? = null,
    @SerializedName("rawResponse")
    val rawResponse : String? = null,
    @SerializedName("payment_ref_id")
    val paymentRefId : String? = null,
    @SerializedName("payment_purpose")
    val paymentPurpose : String? = null,
    @SerializedName("cus_wallet_no")
    val cusWalletNo : String? = null,
    @SerializedName("payment_cus_id")
    val paymentCusId : String? = null,
    @SerializedName("statusCode")
    val statusCode : String? = null,
    @SerializedName("statusMessage")
    val statusMessage : String? = null,
) : PubSubBaseRequest()