package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelAddToPlaylistBean
import com.google.gson.annotations.SerializedName

data class MyChannelAddToPlaylistResponse(
    @SerializedName("response")
    val response: MyChannelAddToPlaylistBean
) : BaseResponse()