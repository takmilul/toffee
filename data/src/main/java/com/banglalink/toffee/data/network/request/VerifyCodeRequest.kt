package com.banglalink.toffee.data.network.request

data class VerifyCodeRequest(
    val code: String,
    val regSessionToken:String,
    val referralCode:String,
    val fcmToken: String,
    val lat: String,
    val lon: String,
    val parentId: Int = 1
) : BaseRequest("confirmCodeV2")