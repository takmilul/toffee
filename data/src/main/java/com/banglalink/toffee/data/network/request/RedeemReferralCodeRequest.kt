package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

class RedeemReferralCodeRequest(
    @SerializedName("referralCode")
    val referralCode: String,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String
) : BaseRequest(apiName = "redeemReferralCode")