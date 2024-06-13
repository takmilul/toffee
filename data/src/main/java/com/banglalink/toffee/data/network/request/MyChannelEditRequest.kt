package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelEditRequest(
    @SerialName("customerId")
    var customerId: Int,
    @SerialName("password")
    var password: String,
    @SerialName("channelId")
    var channelId: Int,
    @SerialName("categoryId")
    val categoryId: Long,
    @SerialName("channelName")
    val channelName: String,
    @SerialName("channelDesc")
    val channelDesc: String? = null,
    @SerialName("oldBannerImageUrl")
    val oldBannerImageUrl: String? = "NULL",
    @SerialName("bannerImage")
    val bannerImage: String? = "NULL",
    @SerialName("oldProfileImageUrl")
    val oldProfileImageUrl: String? = "NULL",
    @SerialName("profileImage")
    val profileImage: String? = "NULL",
    @SerialName("name")
    val name: String = "",
    @SerialName("email")
    val email: String = "",
    @SerialName("address")
    val address: String = "",
    @SerialName("dateOfBirth")
    val dateOfBirth: String = "",
    @SerialName("nationalIdNo")
    val nationalIdNo: String = "",
    @SerialName("paymentPhoneNo")
    val paymentPhoneNo: String = "",
    @SerialName("paymentMethodId")
    val paymentMethodId: Int = 0,
    @SerialName("isBillingInfoUpdated")
    val isBillingInfoUpdated: Boolean = false,
    @SerialName("isChannelInfoUpdate")
    val isChannelInfoUpdate: Boolean = false
) : BaseRequest(ApiNames.EDIT_MY_CHANNEL)