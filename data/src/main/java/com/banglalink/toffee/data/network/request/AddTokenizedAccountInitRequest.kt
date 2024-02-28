package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddTokenizedAccountInitRequest(
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
    val cusEmail: String? = null
) : BaseRequest(ApiNames.SUBSCRIBER_ADD_TOKENIZED_ACCOUNT_INIT)