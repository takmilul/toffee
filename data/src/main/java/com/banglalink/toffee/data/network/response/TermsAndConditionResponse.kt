package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.TermsAndCondition
import com.google.gson.annotations.SerializedName

data class TermsAndConditionResponse(
    @SerializedName("response")
    val response: TermsAndCondition
) : BaseResponse()