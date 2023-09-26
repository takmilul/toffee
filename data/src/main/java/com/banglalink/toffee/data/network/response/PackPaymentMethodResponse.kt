package com.banglalink.toffee.data.network.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

data class PackPaymentMethodResponse(
    @SerializedName("apiLanguage")
    val apiLanguage: String,
    @SerializedName("debugCode")
    val debugCode: Int,
    @SerializedName("debugMsg")
    val debugMsg: String,
    @SerializedName("response")
    val response: PackPaymentMethodBean,
) : BaseResponse()

@Parcelize
data class PackPaymentMethodBean(
    @SerializedName("BKASH")
    val bkash: Bkash?,
    @SerializedName("BL")
    val bl: @RawValue Bl?,
    @SerializedName("FREE")
    val free: @RawValue List<PackPaymentMethod>?,
    @SerializedName("VOUCHER")
    val Voucher: @RawValue List<PackPaymentMethod>?,
    @SerializedName("SSL")
    val ssl: SSL?,
) : Parcelable

@Parcelize
data class Bkash(
    @SerializedName("minimum_price")
    val minimumPrice: Int,
    @SerializedName("BL")
    val blPacks: List<PackPaymentMethod>?,
    @SerializedName("NON_BL")
    val nonBlPacks: List<PackPaymentMethod>?,
) : Parcelable

@Parcelize
data class SSL(
    @SerializedName("BL")
    val blPacks: List<PackPaymentMethod>?,
    @SerializedName("NON_BL")
    val nonBlPacks: List<PackPaymentMethod>?,
) : Parcelable

@Parcelize
data class Bl(
    @SerializedName("minimum_price")
    val minimumPrice: Int,
    @SerializedName("POSTPAID")
    val postpaid: List<PackPaymentMethod>?,
    @SerializedName("PREPAID")
    val prepaid: List<PackPaymentMethod>?,
) : Parcelable


@Parcelize
data class PackPaymentMethod(
    @SerializedName("data_pack_id")
    val dataPackId: Int? = null,
    @SerializedName("payment_method_id")
    val paymentMethodId: Int? = null,
    @SerializedName("is_non_bl_free")
    val isNonBlFree: Int? = null,
    @SerializedName("pack_code")
    val packCode: String? = null,
    @SerializedName("pack_details")
    val packDetails: String? = null,
    @SerializedName("pack_price")
    val packPrice: Int? = null,
    @SerializedName("pack_duration")
    val packDuration: Int? = null,
    @SerializedName("sort_by_code")
    val sortByCode: Int? = null,
    @SerializedName("is_prepaid")
    val isPrepaid: Int? = null,
    @SerializedName("listTitle")
    val listTitle: String? = null,
    @SerializedName("is_auto_renew")
    val isAutoRenew: Int? = null,
    @SerializedName("partner_id")
    val partnerId: Int? = null,
    @SerializedName("campaigns_id")
    val campaignsId: Int? = null,
    @SerializedName("data_pack_cta_button")
    val dataPackCtaButton: Int? = null
    ) : Parcelable