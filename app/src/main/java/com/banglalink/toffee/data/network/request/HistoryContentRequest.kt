package com.banglalink.toffee.data.network.request

data class HistoryContentRequest(
    val customerId:Int,
    val password:String,
    val offset:Int,
    val limit:Int=10,
    val deviceType: Int = 1
) : BaseRequest("getHistoryContents")