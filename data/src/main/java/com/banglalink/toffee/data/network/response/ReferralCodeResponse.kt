package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ReferralCodeBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReferralCodeResponse(
    @SerialName("response")
    val response: ReferralCodeBean? = null
) : BaseResponse()