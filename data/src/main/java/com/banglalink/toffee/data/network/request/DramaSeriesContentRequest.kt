package com.banglalink.toffee.data.network.request

data class DramaSeriesContentRequest (
    val customerId:Int,
    val password:String,
): BaseRequest("getUgcLatestDramaSerial")