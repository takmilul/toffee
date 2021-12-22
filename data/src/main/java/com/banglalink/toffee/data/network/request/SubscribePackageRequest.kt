package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class SubscribePackageRequest(
    @SerializedName("packageId")
    val packageId: Int,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("isAutoRenew")
    val isAutoRenew: String = "false"
) : BaseRequest(ApiNames.SUBSCRIBER_A_PACKAGE)