package com.banglalink.toffee.data.network.request

data class MyChannelPlaylistDeleteRequest(
    val playlistId: Int,
    val customerId: Int,
    val password: String
): BaseRequest("ugcDeletePlayListName")