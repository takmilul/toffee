package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistCreateRequest(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("channelId")
    val channelId: Int,
    @SerialName("isChannelOwner")
    val isChannelOwner: Int,
    @SerialName("playlistName")
    val playlistName: String?,
    @SerialName("isUserPlaylist")
    val isUserPlaylist: Int = 0
) : BaseRequest(ApiNames.CREATE_PLAYLIST)
