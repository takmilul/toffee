package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FireworkRequest(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password:String,
    @SerialName("type")
    val type:String="Live"
) : BaseRequest(ApiNames.GET_FIREWORK_LIST)