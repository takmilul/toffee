package com.banglalink.toffee.data.network.request

data class MyChannelPlaylistVideoDeleteRequest(
    val channelId: Int,
    val playlistContentId: Int,
    val customerId: Int,
    val password: String
): BaseRequest("ugcDeleteContentToPlaylist")