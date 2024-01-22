package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.RedeemReferralCodeBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RedeemReferralCodeResponse(
    @SerialName("response")
    val response: RedeemReferralCodeBean
) : BaseResponse()