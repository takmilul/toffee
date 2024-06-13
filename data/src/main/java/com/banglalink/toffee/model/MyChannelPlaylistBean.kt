package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistBean(
    @SerialName("count")
    val count: Int = 0,
    @SerialName("totalCount")
    val totalCount: Int = 0,
    @SerialName("isOwner")
    val isOwner: Int = 0,
    @SerialName("channelId")
    val channelId: Int = 0,
    @SerialName("playlistNames")
    val channelPlaylist: List<MyChannelPlaylist>? = null,
)