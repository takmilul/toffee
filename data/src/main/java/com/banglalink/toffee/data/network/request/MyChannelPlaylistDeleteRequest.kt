package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistDeleteRequest(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("playlistId")
    val playlistId: Int,
    @SerialName("isUserPlaylist")
    val isUserPlaylist: Int = 0
) : BaseRequest(ApiNames.DELETE_PLAYLIST)