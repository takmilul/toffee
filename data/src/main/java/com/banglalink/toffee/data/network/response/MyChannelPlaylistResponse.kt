package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelPlaylistBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistResponse(
    @SerialName("response")
    val response: MyChannelPlaylistBean,
    @SerialName("isOwner")
    val isOwner: Int,
    @SerialName("channelId")
    val channelId: String
) : BaseResponse()