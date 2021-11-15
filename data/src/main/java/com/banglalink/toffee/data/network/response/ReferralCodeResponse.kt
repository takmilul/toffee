package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ReferralCodeBean
import com.google.gson.annotations.SerializedName

data class ReferralCodeResponse(
    @SerializedName("response")
    val response: ReferralCodeBean
) : BaseResponse()