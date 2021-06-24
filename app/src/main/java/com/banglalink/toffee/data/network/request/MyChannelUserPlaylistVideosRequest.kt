package com.banglalink.toffee.data.network.request

data class MyChannelUserPlaylistVideosRequest (
    val customerId: Int,
    val password: String,
): BaseRequest("getUgcContentByUserPlaylist")