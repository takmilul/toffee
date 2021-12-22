package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class ReferralCodeStatusRequest(
    @SerializedName("phoneNo")
    val phoneNumber: String,
    @SerializedName("referralCode")
    val referralCode: String
) : BaseRequest(ApiNames.GET_REFERRAL_CODE_STATUS)