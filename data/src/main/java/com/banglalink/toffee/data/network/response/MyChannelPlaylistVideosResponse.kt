package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelPlaylistVideosBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistVideosResponse(
    @SerialName("response")
    val response: MyChannelPlaylistVideosBean
) : BaseResponse()