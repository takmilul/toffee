package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelPlaylistEditBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistEditResponse(
    @SerialName("response")
    val response: MyChannelPlaylistEditBean? = null
) : BaseResponse()