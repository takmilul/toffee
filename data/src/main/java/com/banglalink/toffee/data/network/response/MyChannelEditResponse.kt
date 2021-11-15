package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelEditBean
import com.google.gson.annotations.SerializedName

data class MyChannelEditResponse(
    @SerializedName("response")
    val response: MyChannelEditBean
) : BaseResponse()