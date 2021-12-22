package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class MyChannelAddToPlaylistRequest(
    @SerializedName("playlistId")
    val playlistId: Int,
    @SerializedName("contentId")
    val contentId: Int,
    @SerializedName("channelId")
    val channelId: Int,
    @SerializedName("isOwner")
    val isOwner: Int,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("isUserPlaylist")
    val isUserPlaylist: Int = 0
) : BaseRequest(ApiNames.UGC_APP_TO_MY_PLAYLIST)