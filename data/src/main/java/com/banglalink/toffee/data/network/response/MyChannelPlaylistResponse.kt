package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelPlaylistBean
import com.google.gson.annotations.SerializedName

data class MyChannelPlaylistResponse(
    @SerializedName("response")
    val response: MyChannelPlaylistBean
) : BaseResponse()