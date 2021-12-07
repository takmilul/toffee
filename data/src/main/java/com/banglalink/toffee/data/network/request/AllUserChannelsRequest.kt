package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class AllUserChannelsRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String
) : BaseRequest("getUgcAllUserChannel")