package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelDeletePlaylistVideoBean
import com.google.gson.annotations.SerializedName


data class MyChannelPlaylistVideoDeleteResponse(
    @SerializedName("response")
    val response: MyChannelDeletePlaylistVideoBean
) : BaseResponse()