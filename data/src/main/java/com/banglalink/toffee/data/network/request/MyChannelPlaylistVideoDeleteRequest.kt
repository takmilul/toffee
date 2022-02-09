package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class MyChannelPlaylistVideoDeleteRequest(
    @SerializedName("channelId")
    val channelId: Int,
    @SerializedName("playlistContentId")
    val playlistContentId: Int,
    @SerializedName("playlistId")
    val playlistId: Int,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String
) : BaseRequest(ApiNames.DELETE_PLAYLIST_VIDEO)