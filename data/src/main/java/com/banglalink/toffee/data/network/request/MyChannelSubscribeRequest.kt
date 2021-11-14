package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class MyChannelSubscribeRequest(
    @SerializedName("channelId")
    val channelId: Int,
    @SerializedName("isSubscribed")
    val isSubscribed: Int,
    @SerializedName("channelOwnerId")
    val channelOwnerId: Int,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String
) : BaseRequest("ugcSubscribeOnChannel")