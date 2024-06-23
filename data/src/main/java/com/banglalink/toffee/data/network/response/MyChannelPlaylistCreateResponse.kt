package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelPlaylistCreateBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistCreateResponse(
    @SerialName("response")
    val response: MyChannelPlaylistCreateBean? = null
) : BaseResponse()