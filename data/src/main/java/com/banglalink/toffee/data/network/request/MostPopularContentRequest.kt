package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class MostPopularContentRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("telcoId")
    val telcoId: Int = 1
) : BaseRequest("getUgcMostPopularContents")