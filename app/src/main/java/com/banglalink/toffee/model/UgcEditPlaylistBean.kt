package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class UgcEditPlaylistBean (
    val message: String,
    @SerializedName("playlist_name_id")
    val playlistId: Int,
    val messageType: String,
    val systemTime: String
)