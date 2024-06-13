package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReferralCodeStatusRequest(
    @SerialName("phoneNo")
    val phoneNumber: String,
    @SerialName("referralCode")
    val referralCode: String
) : BaseRequest(ApiNames.GET_REFERRAL_CODE_STATUS)