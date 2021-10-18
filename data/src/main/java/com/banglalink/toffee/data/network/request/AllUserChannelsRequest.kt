package com.banglalink.toffee.data.network.request

data class AllUserChannelsRequest(
    val customerId:Int,
    val password:String
): BaseRequest("getUgcAllUserChannel")