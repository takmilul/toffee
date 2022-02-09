package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelPlaylistEditBean
import com.google.gson.annotations.SerializedName

data class MyChannelPlaylistEditResponse(
    @SerializedName("response")
    val response: MyChannelPlaylistEditBean
) : BaseResponse()