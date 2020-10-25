package com.banglalink.toffee.data.network.request

data class MyChannelPlaylistVideosRequest (
    val customerId: Int,
    val password: String,
): BaseRequest("getUgcContentByPlaylist")