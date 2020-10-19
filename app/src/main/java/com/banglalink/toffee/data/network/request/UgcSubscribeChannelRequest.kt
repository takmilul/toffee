package com.banglalink.toffee.data.network.request

data class UgcSubscribeChannelRequest(
    val channelId: Int,
    val isSubscribed: Int,
    val customerId: Int,
    val password: String
): BaseRequest("ugcSubscribeOnChannel")