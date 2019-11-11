package com.banglalink.toffee.data.network.request

class VerifyCodeRequest(
    val code: String,
    val fcmToken: String,
    val lat: String,
    val lon: String,
    val deviceType: Int = 1,
    val parentId: Int = 1
) : BaseRequest("confirmCode")