package com.banglalink.toffee.data.network.response

import android.os.Parcelable
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
    val bkash: PackPaymentMethodData? = null,
    @SerialName("BL")
    val bl: @RawValue PackPaymentMethodData? = null,
    @SerialName("FREE")
    val free: @RawValue PackPaymentMethodData? = null,
    @SerialName("VOUCHER")
    val voucher: @RawValue PackPaymentMethodData? = null,
    @SerialName("SSL")
    val ssl: PackPaymentMethodData? = null,
    @SerialName("NAGAD")
    val nagad: PackPaymentMethodData? = null,
) : Parcelable

@Parcelize
@Serializable
data class PackPaymentMethodData(
    @SerialName("payment_method_name")
    var paymentMethodName: String? = null,
    @SerialName("payment_headline")
    val paymentHeadline : String? = null,
    @SerialName("payment_headline_for_bl")
    val paymentHeadlineForBl: String? = null,
    @SerialName("payment_headline_for_non_bl")
    val paymentHeadlineForNonBl: String? = null,
    @SerialName("payment_sub_headline_one_for_bl")
    val paymentSubHeadlineOneForBl : String? = null,
    @SerialName("payment_sub_headline_one_for_non_bl")
    val paymentSubHeadlineOneForNonBl : String? = null,
    @SerialName("payment_sub_headline_one_for_prepaid")
    var paymentSubHeadlineOneForPrepaid : String? = null,
    @SerialName("payment_sub_headline_one_for_postpaid")
    var paymentSubHeadlineOneForPostpaid : String? = null,
    @SerialName("payment_method_logo_mobile" )
    var paymentMethodLogoMobile : String? = null,
    @SerialName("order_index")
    val orderIndex : Int? = null,
    @SerialName("data")
    val data : List<PackPaymentMethod>? = null,
    @SerialName("BL")
    val blPacks: List<PackPaymentMethod>? = null,
    @SerialName("NON_BL")
    val nonBlPacks: List<PackPaymentMethod>? = null,
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
    @SerialName("is_allow_from_outside" )
    val isAllowFromOutside : Int? = null,
) : Parcelable