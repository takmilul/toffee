package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.UserChannelBean
import com.google.gson.annotations.SerializedName

data class PopularChannelsResponse(
    @SerializedName("response")
    val response: UserChannelBean
) : BaseResponse()