package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.TrendingChannelBean
import com.google.gson.annotations.SerializedName

data class AllUserChannelsResponse(
    @SerializedName("response")
    val response: TrendingChannelBean
) : BaseResponse()