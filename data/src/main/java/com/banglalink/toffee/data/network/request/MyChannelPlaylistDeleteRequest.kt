package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class MyChannelPlaylistDeleteRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("playlistId")
    val playlistId: Int,
    @SerializedName("isUserPlaylist")
    val isUserPlaylist: Int = 0
) : BaseRequest(ApiNames.DELETE_PLAYLIST)