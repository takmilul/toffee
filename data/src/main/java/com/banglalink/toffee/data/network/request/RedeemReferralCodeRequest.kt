package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

class RedeemReferralCodeRequest(
    @SerializedName("referralCode")
    val referralCode: String,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String
) : BaseRequest(apiName = ApiNames.REDEEM_REFERRAL_CODE)