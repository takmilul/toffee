package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.PAYMENT_LOG_FROM_DEVICE
import com.banglalink.toffee.notification.PubSubMessageUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SendPaymentLogFromDeviceEvent @Inject constructor(
    private val json: Json,
) {

    fun execute(paymentLogFromDeviceData: PaymentLogFromDeviceData) {
        PubSubMessageUtil.send(paymentLogFromDeviceData, PAYMENT_LOG_FROM_DEVICE)
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
    @SerialName("transactionStatus")
    val transactionStatus : String? = null,
    @SerialName("amount")
    val amount : String? = null,
    @SerialName("merchantInvoiceNumber")
    val merchantInvoiceNumber : String? = null,
    @SerialName("rawResponse")
    val rawResponse : String? = null,
    @SerialName("statusCode")
    val statusCode : String? = null,
    @SerialName("statusMessage")
    val statusMessage : String? = null,
) : PubSubBaseRequest()