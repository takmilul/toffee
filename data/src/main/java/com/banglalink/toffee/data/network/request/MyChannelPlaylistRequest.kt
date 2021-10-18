package com.banglalink.toffee.data.network.request

data class MyChannelPlaylistRequest(
    val customerId:Int,
    val password:String
): BaseRequest("getUgcPlaylistNames")