package com.banglalink.toffee.data.network.request

data class MyChannelPlaylistEditRequest (
    val customerId:Int,
    val password:String,
    val playlistId: Int,
    val playlistName: String,
    val channelId: Int,
    val isChannelOwner: Int,
    val isUserPlaylist: Int = 0
): BaseRequest("ugcEditPlaylistName")
