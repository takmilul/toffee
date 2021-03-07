package com.banglalink.toffee.data.network.request

data class PopularChannelsRequest(
    val customerId:Int,
    val password:String
) : BaseRequest("getUgcPopularChennel")