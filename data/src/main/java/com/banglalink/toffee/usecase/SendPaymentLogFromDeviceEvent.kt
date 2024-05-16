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
    @SerialName("request_id")
    val requestId : String? = null,
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
    @SerialName("campaign_type")
    val campaignType : String? = null,
    @SerialName("partner_name")
    val partnerName : String? = null,
    @SerialName("partner_id")
    val partnerId : Int? = 0,
    @SerialName("campaign_name")
    val campaignName : String? = null,
    @SerialName("campaign_id")
    val campaignId : Int? = 0,
    @SerialName("campaign_expire_date")
    val campaignExpireDate : String? = null,
    @SerialName("discount")
    val discount : Int? = 0,
    @SerialName("original_price")
    val originalPrice : Int? = 0,
    @SerialName("dob_price")
    val dobPrice: String? = null,
    @SerialName("dob_cp_id")
    val dobCpId: String? = null,
    @SerialName("dob_subs_offer_id")
    val dobSubsOfferId: String? = null,
) : PubSubBaseRequest()
