package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.android.gms.common.annotation.KeepName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@KeepName
@Parcelize
@Serializable
data class MyChannelDetail(
    @SerialName("id")
    val id: Long,
    @SerialName("channel_name")
    val channelName: String? = null,
    @SerialName("channel_desc")
    val description: String? = null,
    @SerialName("profile_url")
    val profileUrl: String? = null,
    @SerialName("banner_url")
    val bannerUrl: String? = null,
    @SerialName("category_id")
    val categoryId: Long = 0,
    @SerialName("name")
    var name: String? = null,
    @SerialName("email")
    var email: String? = null,
    @SerialName("address")
    var address: String? = null,
    @SerialName("date_of_birth")
    val dateOfBirth: String? = null,
    @SerialName("national_id_no")
    val nationalIdNo: String? = null,
    @SerialName("payment_phone_no")
    var paymentPhoneNo: String? = null,
    @SerialName("payment_method_id")
    val paymentMethodId: Long = 0,
    @SerialName("is_email_verified")
    val is_email_verified: Int = 0,
    @SerialName("is_nid_verified")
    val is_nid_verified: Int = 0,
    @SerialName("is_approved")
    val isApproved: Boolean = false,
    @SerialName("channel_share_url")
    val channelShareUrl: String? = null,
) : Parcelable {
    fun isEmailVerified() = is_email_verified == 1
    fun isNIDVerified() = is_nid_verified == 1
}