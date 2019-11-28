package com.banglalink.toffee.data.network.request

data class VerifyCodeRequest(
    val code: String,
    val fcmToken: String,
    val lat: String,
    val lon: String,
    val parentId: Int = 1
) : BaseRequest("confirmCode")