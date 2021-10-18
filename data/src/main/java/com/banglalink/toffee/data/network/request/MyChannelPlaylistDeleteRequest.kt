package com.banglalink.toffee.data.network.request

data class MyChannelPlaylistDeleteRequest(
    val customerId: Int,
    val password: String,
    val playlistId: Int,
    val isUserPlaylist: Int = 0
): BaseRequest("ugcDeletePlayListName")