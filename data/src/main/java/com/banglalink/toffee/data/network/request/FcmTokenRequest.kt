package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class FcmTokenRequest(
    @SerializedName("token")
    val token: String,
    @SerializedName("customerId")
    val customerId: Int
) : BaseRequest(ApiNames.SET_FCM_TOKEN)