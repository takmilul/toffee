package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelDetailBean
import com.google.gson.annotations.SerializedName

class MyChannelDetailResponse(
    @SerializedName("response")
    val response: MyChannelDetailBean
) : BaseResponse()