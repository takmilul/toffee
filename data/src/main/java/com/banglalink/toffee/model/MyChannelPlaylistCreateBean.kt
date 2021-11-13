package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MyChannelPlaylistCreateBean (
    @SerializedName("message")
    val message: String?,
    @SerializedName("messageType")
    val messageType: String?,
    @SerializedName("systemTime")
    val systemTime: String?,
    @SerializedName("playlist_name_id")
    val playlistNameId: Int,
    @SerializedName("channel_id")
    val channelId: Int
)