package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class VoucherPaymentBean (

    @SerialName("is_valid_voucher")
    val isValidVoucher: Boolean = false,
    @SerialName("partner_type")
    val partnerType: String? = null,
    @SerialName("partner_name")
    val partnerName: String? = null,
    @SerialName("partner_id")
    val partnerId: Int? = null,
    @SerialName("partner_campaigns_name")
    val partnerCampaignsName: String? = null,
    @SerialName("partner_campaigns_id")
    val partnerCampaignsId: Int? = null,
    @SerialName("campaigns_duration")
    val campaignsDuration: Int = 0,
    @SerialName("campaigns_expire_date")
    val campaignsExpireDate: String? = null,
    @SerialName("message")
    val message: String? = null

): Parcelable