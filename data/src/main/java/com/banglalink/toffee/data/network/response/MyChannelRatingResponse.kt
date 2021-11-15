package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.MyChannelRatingBean
import com.google.gson.annotations.SerializedName

data class MyChannelRatingResponse(
    @SerializedName("response")
    val response: MyChannelRatingBean
) : BaseResponse()