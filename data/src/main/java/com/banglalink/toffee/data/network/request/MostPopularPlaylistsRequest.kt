package com.banglalink.toffee.data.network.request

data class MostPopularPlaylistsRequest(
    val customerId:Int,
    val password:String
) : BaseRequest("getUgcPopularPlaylistNames")