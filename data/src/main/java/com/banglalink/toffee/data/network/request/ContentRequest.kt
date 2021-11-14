package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class ContentRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("telcoId")
    val telcoId: Int = 1,
) : BaseRequest("getUgcContentsV5")