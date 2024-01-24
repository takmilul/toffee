package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistCreateBean (
    @SerialName("message")
    val message: String? = null,
    @SerialName("messageType")
    val messageType: String? = null,
    @SerialName("systemTime")
    val systemTime: String? = null,
    @SerialName("playlist_name_id")
    val playlistNameId: Int = 0,
    @SerialName("channel_id")
    val channelId: Int = 0
)