package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelDeletePlaylistBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistDeleteResponse(
    @SerialName("response")
    val response: MyChannelDeletePlaylistBean? = null
) : BaseResponse()