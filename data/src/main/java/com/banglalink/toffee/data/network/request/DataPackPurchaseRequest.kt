package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.parcelize.RawValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DataPackPurchaseRequest(
    //Common for All (trial, BL, bKash)
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("isBanglalinkNumber")
    val isBanglalinkNumber: Int,
    @SerialName("pack_id")
    val packId: Int,
    @SerialName("payment_method_id")
    val paymentMethodId: Int,
    @SerialName("is_prepaid")
    val isPrepaid: Int,

    
    //Only for BL and Trial Pack
    @SerialName("pack_title")
    val packTitle: String? = null,
    @SerialName("contents")
    val contentList: List<Int>? = null,
    @SerialName("pack_code")
    val packCode: String? = null,
    @SerialName("pack_details")
    val packDetails: String? = null,
    @SerialName("pack_price")
    val packPrice: Int? = null,
    @SerialName("pack_duration")
    val packDuration: Int? = null,
    @SerialName("is_purchase_call_after_recharge")
    val purchaseCallAfterRecharge: Boolean? = false,

    //Only for voucher
    @SerialName("voucher")
    val voucher: String? = null,
    @SerialName("partner_type")
    val partnerType: String? = null,
    @SerialName("partner_name")
    val partnerName: String? = null,
    @SerialName("partner_id")
    val partnerId: Int? = null,
    @SerialName("partner_campaigns_name")
    val partnerCampaignsName: String? = null,
    @SerialName("partner_campaigns_id")
    val partnerCampaignsId: Int? = null,
    @SerialName("campaigns_expire_date")
    val campaignsExpireDate: String? = null,

    
    //Only for bKash
    @SerialName("data_pack_id")
    val bKashDataPackId: Int? = null,
    @SerialName("bkash")
    val bKashRequest: @RawValue BkashDataPackRequest? = null,
) : BaseRequest(ApiNames.DATA_PACK_PURCHASE)

@Serializable
data class BkashDataPackRequest(
    @SerialName("amount")
    val amount: String? = null,
    @SerialName("createTime")
    val createTime: String? = null,
    @SerialName("currency")
    val currency: String? = null,
    @SerialName("customerMsisdn")
    val customerMsisdn: String? = null,
    @SerialName("errorCode")
    val errorCode: String? = null,
    @SerialName("errorMessage")
    val errorMessage: String? = null,
    @SerialName("intent")
    val intent: String? = null,
    @SerialName("merchantInvoiceNumber")
    val merchantInvoiceNumber: String? = null,
    @SerialName("paymentID")
    val paymentId: String? = null,
    @SerialName("refundAmount")
    val refundAmount: String? = null,
    @SerialName("status")
    val status: Boolean = false,
    @SerialName("transactionStatus")
    val transactionStatus: String? = null,
    @SerialName("trxID")
    val transactionId: String? = null,
    @SerialName("updateTime")
    val updateTime: String? = null
)
