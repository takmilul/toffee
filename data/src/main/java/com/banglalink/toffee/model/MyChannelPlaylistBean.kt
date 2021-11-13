package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MyChannelPlaylistBean(
    @SerializedName("count")
    val count: Int,
    @SerializedName("totalCount")
    val totalCount: Int,
    @SerializedName("isOwner")
    val isOwner: Int,
    @SerializedName("channelId")
    val channelId: Int,
    @SerializedName("playlistNames")
    val channelPlaylist: List<MyChannelPlaylist>?,
)