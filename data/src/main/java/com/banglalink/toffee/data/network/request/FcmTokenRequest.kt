package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FcmTokenRequest(
    @SerialName("token")
    val token: String,
    @SerialName("customerId")
    val customerId: Int
) : BaseRequest(ApiNames.SET_FCM_TOKEN)