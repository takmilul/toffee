package com.banglalink.toffee.model

import com.banglalink.toffee.data.network.response.BodyResponse
import com.google.gson.annotations.SerializedName

class ReferralCodeStatusBean(
    @SerializedName("referralStatus")
    val referralStatus: String,
    @SerializedName("referralStatusMessage")
    val referralStatusMessage: String
) : BodyResponse()