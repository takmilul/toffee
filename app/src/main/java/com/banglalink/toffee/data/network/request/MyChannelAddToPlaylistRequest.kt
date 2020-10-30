package com.banglalink.toffee.data.network.request

data class MyChannelAddToPlaylistRequest(
    val playlistId: Int,
    val contentId: Int,
    val channelId: Int,
    val isOwner: Int,
    val customerId: Int,
    val password: String
): BaseRequest("ugcAddContentToPlaylist")