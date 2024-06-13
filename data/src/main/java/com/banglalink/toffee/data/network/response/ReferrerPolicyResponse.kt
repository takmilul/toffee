package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ReferrerPolicyBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReferrerPolicyResponse(
    @SerialName("response")
    val referrerPolicyBean: ReferrerPolicyBean? = null
) : BaseResponse()