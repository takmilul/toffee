package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class MyChannelPlaylistCreateRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("channelId")
    val channelId: Int,
    @SerializedName("isChannelOwner")
    val isChannelOwner: Int,
    @SerializedName("playlistName")
    val playlistName: String?,
    @SerializedName("isUserPlaylist")
    val isUserPlaylist: Int = 0
) : BaseRequest("ugcCreatePlaylistName")
