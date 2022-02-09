package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelVideosBean
import com.google.gson.annotations.SerializedName

data class MyChannelVideosResponse(
    @SerializedName("response")
    val response: MyChannelVideosBean
) : BaseResponse()