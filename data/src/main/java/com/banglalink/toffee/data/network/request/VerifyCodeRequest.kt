package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class VerifyCodeRequest(
    @SerializedName("code")
    val code: String,
    @SerializedName("regSessionToken")
    val regSessionToken: String,
    @SerializedName("referralCode")
    val referralCode: String,
    @SerializedName("fcmToken")
    val fcmToken: String,
    @SerializedName("lat")
    val lat: String,
    @SerializedName("lon")
    val lon: String,
    @SerializedName("parentId")
    val parentId: Int = 1
) : BaseRequest(ApiNames.VERIFY_OTP)