package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class MyChannelVideoDeleteRequest(
    @SerializedName("contentId")
    val contentId: Int,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String
) : BaseRequest("ugcContentDelete")