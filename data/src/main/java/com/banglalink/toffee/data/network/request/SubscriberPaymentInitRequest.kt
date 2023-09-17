package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class SubscriberPaymentInitRequest(
    @SerializedName("customerId")
    val customerId: Int? = null,
    @SerializedName("password")
    val password: String? = null,
    @SerializedName("is_bl_number")
    val is_Bl_Number : Int? = null,
    @SerializedName("is_prepaid")
    val isPrepaid: Int? = null,
    @SerializedName("pack_id")
    val packId: Int? = null,
    @SerializedName("pack_title")
    val packTitle: String? = null,
    @SerializedName("contents")
    val contents: List<Int>? = null,
    @SerializedName("payment_method_id")
    val paymentMethodId: Int? = null,
    @SerializedName("pack_code")
    val packCode: String? = null,
    @SerializedName("pack_details")
    val packDetails: String? = null,
    @SerializedName("pack_price")
    val packPrice: Int? = null,
    @SerializedName("pack_duration")
    val packDuration: Int? = null,
    @SerializedName("geo_city")
    val geoCity: String? = null,
    @SerializedName("geo_location")
    val geoLocation: String? = null,
    @SerializedName("cus_email")
    val cusEmail: String? = null
) : BaseRequest(ApiNames.SUBSCRIBER_PAYMENT_INIT)