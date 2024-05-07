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
    @SerialName("SYSTEMDISCOUNT")
    val systemDiscount: SystemDiscount? = null,
    @SerialName("DISPLAYMESSAGE")
    val displayMessage: DisplayMessage? = null,
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
    val dataPackId: Int? = 0,
    @SerialName("payment_method_id")
    val paymentMethodId: Int? = 0,
    @SerialName("is_non_bl_free")
    val isNonBlFree: Int? = 0,
    @SerialName("pack_code")
    val packCode: String? = null,
    @SerialName("pack_details")
    val packDetails: String? = null,
    @SerialName("pack_price")
    val packPrice: Int? = 0,
    @SerialName("pack_duration")
    val packDuration: Int? = 0,
    @SerialName("sort_by_code")
    val sortByCode: Int? = 0,
    @SerialName("is_prepaid")
    val isPrepaid: Int? = 0,
    @SerialName("listTitle")
    val listTitle: String? = null,
    @SerialName("is_auto_renew")
    val isAutoRenew: Int? = 0,
    @SerialName("partner_id")
    val partnerId: Int? = 0,
    @SerialName("campaigns_id")
    val campaignsId: Int? = 0,
    @SerialName("data_pack_cta_button")
    val dataPackCtaButton: Int? = 0,
    @SerialName("is_allow_from_outside" )
    val isAllowFromOutside : Int? = 0
) : Parcelable

@Parcelize
@Serializable
data class DisplayMessage(
    @SerialName("top_promotion_msg_bl")
    var top_promotion_msg_bl: String? = null,
    @SerialName("top_promotion_msg_nonbl" )
    var top_promotion_msg_nonbl : String? = null
):Parcelable

@Parcelize
@Serializable
data class SystemDiscount(
    @SerialName("BL"    ) var BL    : DiscountInfo? = null,
    @SerialName("NONBL" ) var NONBL : DiscountInfo? = null,
    @SerialName("BOTH"  ) var BOTH  : DiscountInfo? = null
):Parcelable

@Parcelize
@Serializable
data class DiscountInfo (
    @SerialName("discount_apply_on_payment_method" ) var discountApplyOnPaymentMethod : DiscountApplyOnPaymentMethod? = DiscountApplyOnPaymentMethod(),
    @SerialName("which_payment_method_display"     ) var whichPaymentMethodDisplay    : ArrayList<String>             = arrayListOf(),
    @SerialName("voucher"                          ) var voucher                      : String?                       = null,
    @SerialName("campaign_type"                    ) var campaignType                 : String?                       = null,
    @SerialName("partner_name"                     ) var partnerName                  : String?                       = null,
    @SerialName("partner_id"                       ) var partnerId                    : Int?                          = 0,
    @SerialName("campaign_name"                    ) var campaignName                 : String?                       = null,
    @SerialName("campaign_id"                      ) var campaignId                   : Int?                          = 0,
    @SerialName("campaign_type_id"                 ) var campaignTypeId               : Int?                          = 0,
    @SerialName("campaign_expire_date"             ) var campaignExpireDate           : String?                       = null,
    @SerialName("voucher_generated_type"           ) var voucherGeneratedType         : Int?                          = 0
) : Parcelable

@Parcelize
@Serializable
data class DiscountApplyOnPaymentMethod (
    @SerialName("DCB"   ) var DCB   : String? = null,
    @SerialName("BKASH" ) var BKASH : String? = null,
    @SerialName("SSL"   ) var SSL   : String? = null,
    @SerialName("NAGAD" ) var NAGAD : String? = null
) : Parcelable
