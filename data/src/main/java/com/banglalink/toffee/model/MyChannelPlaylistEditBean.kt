package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MyChannelPlaylistEditBean (
    @SerializedName("message")
    val message: String,
    @SerializedName("playlist_name_id")
    val playlistId: Int,
    @SerializedName("messageType")
    val messageType: String,
    @SerializedName("systemTime")
    val systemTime: String
)