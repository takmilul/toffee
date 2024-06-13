package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistVideoDeleteRequest(
    @SerialName("channelId")
    val channelId: Int,
    @SerialName("playlistContentId")
    val playlistContentId: Int,
    @SerialName("playlistId")
    val playlistId: Int,
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String
) : BaseRequest(ApiNames.DELETE_PLAYLIST_VIDEO)