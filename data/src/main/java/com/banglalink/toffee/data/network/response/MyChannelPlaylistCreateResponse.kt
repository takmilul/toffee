package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelPlaylistCreateBean
import com.google.gson.annotations.SerializedName

data class MyChannelPlaylistCreateResponse(
    @SerializedName("response")
    val response: MyChannelPlaylistCreateBean
) : BaseResponse()