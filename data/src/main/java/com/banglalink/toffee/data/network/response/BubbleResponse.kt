package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.RamadanScheduledResponse
import com.google.gson.annotations.SerializedName

data class BubbleResponse(
    @SerializedName("response")
    val response: RamadanScheduledResponse,
) : BaseResponse()
