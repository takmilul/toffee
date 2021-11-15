package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ReferralCodeStatusBean
import com.google.gson.annotations.SerializedName

class ReferralCodeStatusResponse(
    @SerializedName("response")
    val response: ReferralCodeStatusBean
) : BaseResponse()