package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelPlaylistVideosBean
import com.google.gson.annotations.SerializedName

data class MyChannelPlaylistVideosResponse(
    @SerializedName("response")
    val response: MyChannelPlaylistVideosBean
) : BaseResponse()