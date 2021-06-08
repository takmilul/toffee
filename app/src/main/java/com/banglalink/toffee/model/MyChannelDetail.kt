package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MyChannelDetail(
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
    val isEmailVerified: Boolean = false,
    @SerializedName("is_nid_verified")
    val isNIDVerified: Boolean = false,
) : Parcelable