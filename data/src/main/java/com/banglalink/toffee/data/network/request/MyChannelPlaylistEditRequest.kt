package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistEditRequest(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("playlistId")
    val playlistId: Int,
    @SerialName("playlistName")
    val playlistName: String,
    @SerialName("channelId")
    val channelId: Int,
    @SerialName("isChannelOwner")
    val isChannelOwner: Int,
    @SerialName("isUserPlaylist")
    val isUserPlaylist: Int = 0
) : BaseRequest(ApiNames.EDIT_PLAYLIST)
