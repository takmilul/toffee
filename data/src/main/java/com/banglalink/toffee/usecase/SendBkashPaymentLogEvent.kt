package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.BKASH_PAYMENT_LOG
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendBkashPaymentLogEvent @Inject constructor() {
    private val gson = Gson()

    fun execute(bkashPaymentLogData: BkashPaymentLogData) {
        PubSubMessageUtil.sendMessage(gson.toJson(bkashPaymentLogData), BKASH_PAYMENT_LOG)
    }
}

data class BkashPaymentLogData(
    @SerializedName("id")
    val id : String? = null,
    @SerializedName("calling_api_name")
    val callingApiName : String? = null,
    @SerializedName("user_id")
    val userId : Int? = null,
    @SerializedName("pack_id")
    val packId : Int? = null,
    @SerializedName("pack_title")
    val packTitle : String? = null,
    @SerializedName("data_pack_id")
    val dataPackId : Int? = null,
    @SerializedName("data_pack_details")
    val dataPackDetails : String? = null,
    @SerializedName("payment_method_id")
    val paymentMethodId : String? = null,
    @SerializedName("login_msisdn")
    val loginMsisdn : String? = null,
    @SerializedName("payment_msisdn")
    val paymentMsisdn : String? = null,
    @SerializedName("payment_id")
    val paymentId : String? = null,
    @SerializedName("trx_id")
    val trxId : String? = null,
    @SerializedName("transaction_status")
    val transactionStatus : String? = null,
    @SerializedName("amount")
    val amount : String? = null,
    @SerializedName("merchant_invoice_number")
    val merchantInvoiceNumber : String? = null,
    @SerializedName("raw_response")
    val rawResponse : String? = null
    ) : PubSubBaseRequest()
