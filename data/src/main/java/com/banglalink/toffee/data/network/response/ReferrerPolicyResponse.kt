package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ReferrerPolicyBean
import com.google.gson.annotations.SerializedName

data class ReferrerPolicyResponse(
    @SerializedName("response")
    val referrerPolicyBean: ReferrerPolicyBean
) : BaseResponse()