package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ChannelCategoryBean
import com.google.gson.annotations.SerializedName

data class AllChannelResponse(
    @SerializedName("response")
    val response: ChannelCategoryBean
) : BaseResponse()