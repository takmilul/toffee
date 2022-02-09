package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelDeletePlaylistBean
import com.google.gson.annotations.SerializedName


data class MyChannelPlaylistDeleteResponse(
    @SerializedName("response")
    val response: MyChannelDeletePlaylistBean
) : BaseResponse()