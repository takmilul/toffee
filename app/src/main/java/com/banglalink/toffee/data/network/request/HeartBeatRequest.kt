package com.banglalink.toffee.data.network.request

data class HeartBeatRequest(
    val customerId: Int,
    val password:String,
    val lat: String,
    val lon: String,
    val type: String = "BACKGROUND"
):BaseRequest("heartBeat")