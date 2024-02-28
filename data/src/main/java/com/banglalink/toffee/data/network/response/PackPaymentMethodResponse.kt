package com.banglalink.toffee.data.network.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackPaymentMethodResponse(
    @SerialName("apiLanguage")
    val apiLanguage: String? = null,
    @SerialName("debugCode")
    val debugCode: Int = 0,
    @SerialName("debugMsg")
    val debugMsg: String? = null,
    @SerialName("response")
    val response: PackPaymentMethodBean,
) : BaseResponse()

@Parcelize
@Serializable
data class PackPaymentMethodBean(
    @SerialName("BKASH")
    val bkash: Bkash? = null,
    @SerialName("BL")
    val bl: @RawValue Bl? = null,
    @SerialName("FREE")
    val free: @RawValue List<PackPaymentMethod>? = null,
    @SerialName("VOUCHER")
    val Voucher: @RawValue List<PackPaymentMethod>? = null,
    @SerialName("SSL")
    val ssl: SSL? = null,
    @SerialName("NAGAD")
    val nagad: NAGAD? = null,
) : Parcelable

@Parcelize
@Serializable
data class Bkash(
    @SerialName("minimum_price")
    val minimumPrice: Int,
    @SerialName("BL")
    val blPacks: List<PackPaymentMethod>? = null,
    @SerialName("NON_BL")
    val nonBlPacks: List<PackPaymentMethod>? = null,
) : Parcelable

@Parcelize
@Serializable
data class SSL(
    @SerialName("BL")
    val blPacks: List<PackPaymentMethod>? = null,
    @SerialName("NON_BL")
    val nonBlPacks: List<PackPaymentMethod>? = null,
) : Parcelable

@Parcelize
@Serializable
data class NAGAD(
    @SerialName("BL")
    val blPacks: List<PackPaymentMethod>? = null,
    @SerialName("NON_BL")
    val nonBlPacks: List<PackPaymentMethod>? = null,
) : Parcelable

@Parcelize
@Serializable
data class Bl(
    @SerialName("minimum_price")
    val minimumPrice: Int = 0,
    @SerialName("POSTPAID")
    val postpaid: List<PackPaymentMethod>? = null,
    @SerialName("PREPAID")
    val prepaid: List<PackPaymentMethod>? = null,
) : Parcelable

@Parcelize
@Serializable
data class PackPaymentMethod(
    @SerialName("data_pack_id")
    val dataPackId: Int? = null,
    @SerialName("payment_method_id")
    val paymentMethodId: Int? = null,
    @SerialName("is_non_bl_free")
    val isNonBlFree: Int? = null,
    @SerialName("pack_code")
    val packCode: String? = null,
    @SerialName("pack_details")
    val packDetails: String? = null,
    @SerialName("pack_price")
    val packPrice: Int? = null,
    @SerialName("pack_duration")
    val packDuration: Int? = null,
    @SerialName("sort_by_code")
    val sortByCode: Int? = null,
    @SerialName("is_prepaid")
    val isPrepaid: Int? = null,
    @SerialName("listTitle")
    val listTitle: String? = null,
    @SerialName("is_auto_renew")
    val isAutoRenew: Int? = null,
    @SerialName("partner_id")
    val partnerId: Int? = null,
    @SerialName("campaigns_id")
    val campaignsId: Int? = null,
    @SerialName("data_pack_cta_button")
    val dataPackCtaButton: Int? = null,
) : Parcelable