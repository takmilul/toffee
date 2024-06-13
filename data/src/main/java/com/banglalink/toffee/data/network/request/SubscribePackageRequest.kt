package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscribePackageRequest(
    @SerialName("packageId")
    val packageId: Int,
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("isAutoRenew")
    val isAutoRenew: String = "false"
) : BaseRequest(ApiNames.SUBSCRIBE_PACKAGE)