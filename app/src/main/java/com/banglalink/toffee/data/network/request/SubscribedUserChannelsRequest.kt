package com.banglalink.toffee.data.network.request

data class SubscribedUserChannelsRequest(
    val customerId:Int,
    val password:String
): BaseRequest("getUgcChannelSubscriptionList")