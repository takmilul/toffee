package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.RawValue

data class DataPackPurchaseRequest(
    //Common for All (trial, BL, bKash)
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("isBanglalinkNumber")
    val isBanglalinkNumber: Int,
    @SerializedName("pack_id")
    val packId: Int,
    @SerializedName("payment_method_id")
    val paymentMethodId: Int,
    
    //Only for BL Data Pack
    @SerializedName("pack_title")
    val packTitle: String? = null,
    @SerializedName("contents")
    val contentList: List<Int>? = null,
    @SerializedName("pack_code")
    val packCode: String? = null,
    @SerializedName("pack_details")
    val packDetails: String? = null,
    @SerializedName("pack_price")
    val packPrice: Int? = null,
    @SerializedName("pack_duration")
    val packDuration: Int? = null,
    
    //Only for bKash
    @SerializedName("data_pack_id")
    val bKashDataPackId: Int? = null,
    @SerializedName("bkash")
    val bKashRequest: @RawValue BkashDataPackRequest? = null,
) : BaseRequest(ApiNames.DATA_PACK_PURCHASE)

data class BkashDataPackRequest(
    @SerializedName("amount")
    val amount: String? = null,
    @SerializedName("createTime")
    val createTime: String? = null,
    @SerializedName("currency")
    val currency: String? = null,
    @SerializedName("customerMsisdn")
    val customerMsisdn: String? = null,
    @SerializedName("errorCode")
    val errorCode: String? = null,
    @SerializedName("errorMessage")
    val errorMessage: String? = null,
    @SerializedName("intent")
    val intent: String? = null,
    @SerializedName("merchantInvoiceNumber")
    val merchantInvoiceNumber: String? = null,
    @SerializedName("paymentID")
    val paymentId: String? = null,
    @SerializedName("refundAmount")
    val refundAmount: String? = null,
    @SerializedName("status")
    val status: Boolean = false,
    @SerializedName("subscriptionExpireDay")
    val subscriptionExpireDay: String? = null,
    @SerializedName("transactionStatus")
    val transactionStatus: String? = null,
    @SerializedName("trxID")
    val trxID: String? = null,
    @SerializedName("updateTime")
    val updateTime: String? = null
)
