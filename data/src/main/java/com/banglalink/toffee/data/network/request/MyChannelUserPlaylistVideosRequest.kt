package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class MyChannelUserPlaylistVideosRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
) : BaseRequest(ApiNames.GET_USER_CHANNEL_PLAYLIST_VIDEOS)