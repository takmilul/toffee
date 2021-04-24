package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class ApiLoginRequest(
    val customerId: Int,
    val password: String,
    @SerializedName("msisdn")
    val phoneNumber: String,
    val lat: String,
    val lon: String,
    val fcmToken: String
) : BaseRequest("apiLoginV2")