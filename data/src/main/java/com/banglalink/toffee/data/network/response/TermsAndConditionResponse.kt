package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.TermsAndCondition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TermsAndConditionResponse(
    @SerialName("response")
    val response: TermsAndCondition? = null
) : BaseResponse()