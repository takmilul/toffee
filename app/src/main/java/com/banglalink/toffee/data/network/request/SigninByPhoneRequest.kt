package com.banglalink.toffee.data.network.request

data class SigninByPhoneRequest(
    val phoneNo: String,
    val lat: String,
    val lon: String,
    val deviceType: Int = 1,
    val parentId: Int = 1,
    val email: String = "",
    val serviceOperatorType: String = "TELCO",
    val referralCode: String = ""

) : BaseRequest("reRegistration")