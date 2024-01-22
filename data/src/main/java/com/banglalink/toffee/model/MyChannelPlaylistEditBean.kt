package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistEditBean (
    @SerialName("message")
    val message: String,
    @SerialName("playlist_name_id")
    val playlistId: Int,
    @SerialName("messageType")
    val messageType: String,
    @SerialName("systemTime")
    val systemTime: String
)