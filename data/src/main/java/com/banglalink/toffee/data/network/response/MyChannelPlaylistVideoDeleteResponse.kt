package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelDeletePlaylistVideoBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistVideoDeleteResponse(
    @SerialName("response")
    val response: MyChannelDeletePlaylistVideoBean? = null
) : BaseResponse()