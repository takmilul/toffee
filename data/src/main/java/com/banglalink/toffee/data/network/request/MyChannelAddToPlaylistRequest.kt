package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelAddToPlaylistRequest(
    @SerialName("playlistId")
    val playlistId: Int,
    @SerialName("contentId")
    val contentId: Int,
    @SerialName("channelId")
    val channelId: Int,
    @SerialName("isOwner")
    val isOwner: Int,
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("isUserPlaylist")
    val isUserPlaylist: Int = 0
) : BaseRequest(ApiNames.ADD_CONTENT_TO_PLAYLIST)