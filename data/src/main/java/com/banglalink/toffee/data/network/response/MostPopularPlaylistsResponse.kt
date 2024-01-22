package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelPlaylistBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MostPopularPlaylistsResponse(
    @SerialName("response")
    val response: MyChannelPlaylistBean
) : BaseResponse()