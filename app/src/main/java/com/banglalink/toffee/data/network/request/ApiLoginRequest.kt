package com.banglalink.toffee.data.network.request


data class ApiLoginRequest(
    val customerId: Int,
    val password: String,
    val lat: String,
    val lon: String,
    val deviceType: Int =1,
    val fcmToken: String
) : BaseRequest("apiLogin")