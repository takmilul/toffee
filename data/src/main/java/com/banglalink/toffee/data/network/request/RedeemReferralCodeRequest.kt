package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RedeemReferralCodeRequest(
    @SerialName("referralCode")
    val referralCode: String,
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String
) : BaseRequest(apiName = ApiNames.REDEEM_REFERRAL_CODE)