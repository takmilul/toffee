package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MyChannelPlaylistCreateBean (
    val message: String?,
    val messageType: String?,
    val systemTime: String?,
    @SerializedName("playlist_name_id")
    val playlistNameId: Int,
    @SerializedName("channel_id")
    val channelId: Int
)