package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class ApiLoginRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("msisdn")
    val phoneNumber: String,
    @SerializedName("lat")
    val lat: String,
    @SerializedName("lon")
    val lon: String,
    @SerializedName("fcmToken")
    val fcmToken: String
) : BaseRequest("apiLoginV2")