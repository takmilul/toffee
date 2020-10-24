package com.banglalink.toffee.data.network.request

data class MyChannelPlaylistCreateRequest (
    val customerId: Int,
    val password: String,
    val channelId: Int,
    val isChannelOwner: Int,
    val playlistName: String?
): BaseRequest("ugcCreatePlaylistName")
