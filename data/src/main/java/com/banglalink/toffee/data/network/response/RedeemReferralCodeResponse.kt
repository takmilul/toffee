package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.RedeemReferralCodeBean
import com.google.gson.annotations.SerializedName

class RedeemReferralCodeResponse(
    @SerializedName("response")
    val response: RedeemReferralCodeBean
) : BaseResponse()