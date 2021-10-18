package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class ReferrerPolicyRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String
) : BaseRequest("getReferrerPolicy")