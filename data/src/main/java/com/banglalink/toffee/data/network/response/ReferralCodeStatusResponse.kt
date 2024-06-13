package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ReferralCodeStatusBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ReferralCodeStatusResponse(
    @SerialName("response")
    val response: ReferralCodeStatusBean? = null
) : BaseResponse()