package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistBean(
    @SerialName("count")
    val count: Int,
    @SerialName("totalCount")
    val totalCount: Int,
    @SerialName("isOwner")
    val isOwner: Int,
    @SerialName("channelId")
    val channelId: Int,
    @SerialName("playlistNames")
    val channelPlaylist: List<MyChannelPlaylist>?,
)