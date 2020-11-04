package com.banglalink.toffee.data.network.request

data class MyChannelPlaylistEditRequest (
    val playlistId: Int,
    val playlistName: String,
    val channelId: Int,
    val isChannelOwner: Int,
    val customerId:Int,
    val password:String
): BaseRequest("ugcEditPlaylistName")
