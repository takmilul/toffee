package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class MyChannelEditRequest(
    @SerializedName("customerId")
    var customerId: Int,
    @SerializedName("password")
    var password: String,
    @SerializedName("channelId")
    var channelId: Int,
    @SerializedName("categoryId")
    val categoryId: Long,
    @SerializedName("channelName")
    val channelName: String,
    @SerializedName("channelDesc")
    val channelDesc: String? = null,
    @SerializedName("oldBannerImageUrl")
    val oldBannerImageUrl: String? = "NULL",
    @SerializedName("bannerImage")
    val bannerImage: String? = "NULL",
    @SerializedName("oldProfileImageUrl")
    val oldProfileImageUrl: String? = "NULL",
    @SerializedName("profileImage")
    val profileImage: String? = "NULL",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("email")
    val email: String = "",
    @SerializedName("address")
    val address: String = "",
    @SerializedName("dateOfBirth")
    val dateOfBirth: String = "",
    @SerializedName("nationalIdNo")
    val nationalIdNo: String = "",
    @SerializedName("paymentPhoneNo")
    val paymentPhoneNo: String = "",
    @SerializedName("paymentMethodId")
    val paymentMethodId: Int = 0,
    @SerializedName("isBillingInfoUpdated")
    val isBillingInfoUpdated: Boolean = false,
    @SerializedName("isChannelInfoUpdate")
    val isChannelInfoUpdate: Boolean = false
) : BaseRequest("ugcChannelEdit")