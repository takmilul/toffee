package com.banglalink.toffee.data.network.request

data class UgcMyChannelPlaylistRequest(
    val customerId:Int,
    val password:String
): BaseRequest("getUgcPlaylistNames")