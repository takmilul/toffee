package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class FcmTokenRequest(
    @SerializedName("token")
    val token: String,
    @SerializedName("customerId")
    val customerId: Int
) : BaseRequest("setFcmToken")