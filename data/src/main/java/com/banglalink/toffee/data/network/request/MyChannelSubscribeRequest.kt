package com.banglalink.toffee.data.network.request

data class MyChannelSubscribeRequest(
    val channelId: Int,
    val isSubscribed: Int,
    val channelOwnerId: Int,
    val customerId: Int,
    val password: String
): BaseRequest("ugcSubscribeOnChannel")