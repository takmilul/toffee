package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class VoucherPaymentBean (

    @SerializedName("is_valid_voucher")
    val isValidVoucher: Boolean,
    @SerializedName("partner_type")
    val partnerType: String?=null,
    @SerializedName("partner_name")
    val partnerName: String?=null,
    @SerializedName("partner_id")
    val partnerId: Int?=null,
    @SerializedName("partner_campaigns_name")
    val partnerCampaignsName: String?=null,
    @SerializedName("partner_campaigns_id")
    val partnerCampaignsId: Int?=null,
    @SerializedName("campaigns_duration")
    val campaignsDuration: Int,
    @SerializedName("campaigns_expire_date")
    val campaignsExpireDate: String,
    @SerializedName("message")
    val message: String

): Parcelable