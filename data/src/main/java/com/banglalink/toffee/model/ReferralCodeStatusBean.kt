package com.banglalink.toffee.model

import com.banglalink.toffee.data.network.response.BodyResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ReferralCodeStatusBean(
    @SerialName("referralStatus")
    val referralStatus: String,
    @SerialName("referralStatusMessage")
    val referralStatusMessage: String
) : BodyResponse()