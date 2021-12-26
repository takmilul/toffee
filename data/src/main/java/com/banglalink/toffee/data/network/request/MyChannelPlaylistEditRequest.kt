package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class MyChannelPlaylistEditRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("playlistId")
    val playlistId: Int,
    @SerializedName("playlistName")
    val playlistName: String,
    @SerializedName("channelId")
    val channelId: Int,
    @SerializedName("isChannelOwner")
    val isChannelOwner: Int,
    @SerializedName("isUserPlaylist")
    val isUserPlaylist: Int = 0
) : BaseRequest(ApiNames.EDIT_PLAYLIST)
