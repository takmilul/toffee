package com.banglalink.toffee.data.network.request

data class UgcMyChannelDetailRequest(
    val customerId:Int,
    val password:String
): BaseRequest("getUgcChannelDetails")