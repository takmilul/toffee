package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelDeleteVideoBean
import com.google.gson.annotations.SerializedName


data class MyChannelVideoDeleteResponse(
    @SerializedName("response")
    val response: MyChannelDeleteVideoBean
) : BaseResponse()