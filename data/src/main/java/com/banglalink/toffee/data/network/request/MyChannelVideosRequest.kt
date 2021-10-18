package com.banglalink.toffee.data.network.request

data class MyChannelVideosRequest(
    val customerId:Int,
    val password:String,
    override val offset:Int,
    override val limit:Int=10
) : BasePagingRequest("getUgcChannelAllContent")