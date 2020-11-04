package com.banglalink.toffee.data.network.request

data class MostPopularContentRequest(
    val customerId:Int,
    val password:String,
    val telcoId: Int = 1
) : BaseRequest("getUgcMostPopularContents")