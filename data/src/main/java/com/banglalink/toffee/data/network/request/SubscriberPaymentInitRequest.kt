package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriberPaymentInitRequest(
    @SerialName("customerId")
    val customerId: Int? = null,
    @SerialName("password")
    val password: String? = null,
    @SerialName("is_bl_number")
    val is_Bl_Number : Int? = null,
    @SerialName("is_prepaid")
    val isPrepaid: Int? = null,
    @SerialName("pack_id")
    val packId: Int? = null,
    @SerialName("pack_title")
    val packTitle: String? = null,
    @SerialName("contents")
    val contents: List<Int>? = null,
    @SerialName("payment_method_id")
    val paymentMethodId: Int? = null,
    @SerialName("pack_code")
    val packCode: String? = null,
    @SerialName("pack_details")
    val packDetails: String? = null,
    @SerialName("pack_price")
    val packPrice: Int? = null,
    @SerialName("pack_duration")
    val packDuration: Int? = null,
    @SerialName("client_type")
    val clientType: String? = null,
    @SerialName("payment_purpose")
    val paymentPurpose: String? = null,
    @SerialName("payment_token")
    val paymentToken: String? = null,
    @SerialName("geo_city")
    val geoCity: String? = null,
    @SerialName("geo_location")
    val geoLocation: String? = null,
    @SerialName("cus_email")
    val cusEmail: String? = null,


    @SerialName("voucher")
    val voucher: String? = null,
    @SerialName("campaign_type")
    val campaign_type: String? = null,
    @SerialName("partner_name")
    val partner_name: String? = null,
    @SerialName("partner_id")
    val partner_id: Int? = 0,
    @SerialName("campaign_name")
    val campaign_name: String? = null,
    @SerialName("campaign_id")
    val campaign_id: Int? = 0,
    @SerialName("campaign_type_id")
    val campaign_type_id: Int? = 0,
    @SerialName("campaign_expire_date")
    val campaign_expire_date: String? = null,
    @SerialName("voucher_generated_type")
    val voucher_generated_type: Int? = 0,
    @SerialName("discount")
    val discount: Int? = 0,
    @SerialName("original_price")
    val original_price: Int? = 0

) : BaseRequest(ApiNames.SUBSCRIBER_PAYMENT_INIT)