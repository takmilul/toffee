package com.banglalink.toffee.data.network.request

data class UgcGetMyChannelPlaylistRequest(
    val customerId:Int,
    val password:String,
    val offset:Int,
    val limit:Int=10
): BaseRequest("getUgcPlaylistNames")