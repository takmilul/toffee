package com.banglalink.toffee.data.network.request

data class MyChannelDetailRequest(
    val customerId:Int,
    val password:String
): BaseRequest("getUgcChannelDetails")