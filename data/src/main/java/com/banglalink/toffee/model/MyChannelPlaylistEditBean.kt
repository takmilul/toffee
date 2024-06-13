package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistEditBean (
    @SerialName("message")
    val message: String? = null,
    @SerialName("playlist_name_id")
    val playlistId: Int = 0,
    @SerialName("messageType")
    val messageType: String? = null,
    @SerialName("systemTime")
    val systemTime: String? = null
)