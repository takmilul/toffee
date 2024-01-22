package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelAddToPlaylistBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelAddToPlaylistResponse(
    @SerialName("response")
    val response: MyChannelAddToPlaylistBean
) : BaseResponse()