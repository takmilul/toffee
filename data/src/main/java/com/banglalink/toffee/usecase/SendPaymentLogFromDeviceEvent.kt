package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.PAYMENT_LOG_FROM_DEVICE
import com.banglalink.toffee.notification.PubSubMessageUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class SendPaymentLogFromDeviceEvent @Inject constructor() {
    
    fun execute(paymentLogFromDeviceData: PaymentLogFromDeviceData) {
        PubSubMessageUtil.sendMessage(paymentLogFromDeviceData, PAYMENT_LOG_FROM_DEVICE)
    }
}

@Serializable
data class PaymentLogFromDeviceData(
    @SerialName("id")
    val id : Long = 0,
    @SerialName("callingApiName")
    val callingApiName : String? = null,
    @SerialName("packId")
    val packId : Int = 0,
    @SerialName("packTitle")
    val packTitle : String? = null,
    @SerialName("dataPackId")
    val dataPackId : Int = 0,
    @SerialName("dataPackDetails")
    val dataPackDetails : String? = null,
    @SerialName("paymentMethodId")
    val paymentMethodId : Int = 0,
    @SerialName("paymentMsisdn")
    val paymentMsisdn : String? = null,
    @SerialName("paymentId")
    val paymentId : String? = null,
    @SerialName("trxId")
    val transactionId : String? = null,
    @SerialName("requestId")
    val requestId : String? = null, //dcb only
    @SerialName("transactionStatus")
    val transactionStatus : String? = null,
    @SerialName("amount")
    val amount : String? = null,
    @SerialName("merchantInvoiceNumber")
    val merchantInvoiceNumber : String? = null,
    @SerialName("rawResponse")
    val rawResponse : String? = null,
    @SerialName("payment_ref_id")
    val paymentRefId : String? = null,
    @SerialName("payment_purpose")
    val paymentPurpose : String? = null,
    @SerialName("cus_wallet_no")
    val cusWalletNo : String? = null,
    @SerialName("payment_cus_id")
    val paymentCusId : String? = null,
    @SerialName("statusCode")
    val statusCode : String? = null,
    @SerialName("statusMessage")
    val statusMessage : String? = null,
    @SerialName("voucher")
    val voucher : String? = null,
    @SerialName("campaignType")
    val campaignType : String? = null,
    @SerialName("partnerName")
    val partnerName : String? = null,
    @SerialName("partnerId")
    val partnerId : Int? = 0,
    @SerialName("campaignName")
    val campaignName : String? = null,
    @SerialName("campaignId")
    val campaignId : Int? = 0,
    @SerialName("campaignExpireDate")
    val campaignExpireDate : String? = null,
    @SerialName("discount")
    val discount : String? = null,
    @SerialName("originalPrice")
    val originalPrice : String? = null,
    //dcb only
    @SerialName("dobPrice")
    val dobPrice: String? = null,
    @SerialName("dobCpId")
    val dobCpId: String? = null,
    @SerialName("dobSubsOfferId")
    val dobSubsOfferId: String? = null,
) : PubSubBaseRequest()
