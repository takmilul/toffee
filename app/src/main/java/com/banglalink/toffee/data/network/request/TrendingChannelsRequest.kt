package com.banglalink.toffee.data.network.request

data class TrendingChannelsRequest(
    val customerId:Int,
    val password:String
): BaseRequest("getUgcAllUserChannel")