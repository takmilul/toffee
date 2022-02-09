package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

class RedeemReferralCodeBean(
    @SerializedName("referralStatus")
    val referralStatus: String,
    @SerializedName("referralStatusMessage")
    val referralStatusMessage: String
)