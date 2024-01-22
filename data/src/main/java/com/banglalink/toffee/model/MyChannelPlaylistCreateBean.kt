package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistCreateBean (
    @SerialName("message")
    val message: String?,
    @SerialName("messageType")
    val messageType: String?,
    @SerialName("systemTime")
    val systemTime: String?,
    @SerialName("playlist_name_id")
    val playlistNameId: Int,
    @SerialName("channel_id")
    val channelId: Int
)