package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyCodeRequest(
    @SerialName("code")
    val code: String,
    @SerialName("regSessionToken")
    val regSessionToken: String,
    @SerialName("referralCode")
    val referralCode: String,
    @SerialName("fcmToken")
    val fcmToken: String,
    @SerialName("lat")
    val lat: String,
    @SerialName("lon")
    val lon: String,
    @SerialName("parentId")
    val parentId: Int = 1
) : BaseRequest(ApiNames.VERIFY_OTP)