package com.banglalink.toffee.data.network.request

data class MyChannelUserPlaylistRequest(
    val customerId:Int,
    val password:String
): BaseRequest("getUgcUserPlaylistNames")