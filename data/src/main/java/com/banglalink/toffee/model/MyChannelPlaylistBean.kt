package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MyChannelPlaylistBean (
    val count: Int,
    val totalCount: Int,
    val isOwner: Int,
    val channelId: Int,
    @SerializedName("playlistNames")
    val channelPlaylist: List<MyChannelPlaylist>?,
    
)