package com.banglalink.toffee.data.network.request

data class HeartBeatRequest(
    val contentId:Int,
    val contentType:String,
    val customerId: Int,
    val password:String,
    val lat: String,
    val lon: String,
    val isNetworkSwitch:Boolean = false,
    val type: String = "FOREGROUND"
):BaseRequest("heartBeat")