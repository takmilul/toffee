package com.banglalink.toffee.data.network.request

data class UgcPopularChannelsRequest(
    val customerId:Int,
    val password:String
) : BaseRequest("getUgcPopularChennel")