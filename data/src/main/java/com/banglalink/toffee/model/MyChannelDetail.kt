package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.android.gms.common.annotation.KeepName
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@KeepName
@Parcelize
data class MyChannelDetail(
    @SerializedName("id")
    val id: Long,
    @SerializedName("channel_name")
    val channelName: String? = null,
    @SerializedName("channel_desc")
    val description: String? = null,
    @SerializedName("profile_url")
    val profileUrl: String? = null,
    @SerializedName("banner_url")
    val bannerUrl: String? = null,
    @SerializedName("category_id")
    val categoryId: Long = 0,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("email")
    var email: String? = null,
    @SerializedName("address")
    var address: String? = null,
    @SerializedName("date_of_birth")
    val dateOfBirth: String? = null,
    @SerializedName("national_id_no")
    val nationalIdNo: String? = null,
    @SerializedName("payment_phone_no")
    var paymentPhoneNo: String? = null,
    @SerializedName("payment_method_id")
    val paymentMethodId: Long = 0,
    @SerializedName("is_email_verified")
    val is_email_verified: Int = 0,
    @SerializedName("is_nid_verified")
    val is_nid_verified: Int = 0,
    @SerializedName("is_approved")
    val isApproved: Boolean = false,
    @SerializedName("channel_share_url")
    val channelShareUrl: String? = null,
) : Parcelable {
    fun isEmailVerified() = is_email_verified == 1
    fun isNIDVerified() = is_nid_verified == 1
}