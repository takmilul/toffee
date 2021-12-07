package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelSubscribeBean
import com.google.gson.annotations.SerializedName


data class MyChannelSubscribeResponse(
    @SerializedName("response")
    val response: MyChannelSubscribeBean
) : BaseResponse()