package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelPlaylistBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistResponse(
    @SerialName("response")
    val response: MyChannelPlaylistBean? = null,
    @SerialName("isOwner")
    val isOwner: Int = 0,
    @SerialName("channelId")
    val channelId: String? = "0"
) : BaseResponse()