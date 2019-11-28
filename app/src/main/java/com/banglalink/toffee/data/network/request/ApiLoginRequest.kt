package com.banglalink.toffee.data.network.request


data class ApiLoginRequest(
    val customerId: Int,
    val password: String,
    val lat: String,
    val lon: String,
    val fcmToken: String
) : BaseRequest("apiLogin")