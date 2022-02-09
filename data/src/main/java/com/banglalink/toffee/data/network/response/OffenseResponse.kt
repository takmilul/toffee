package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.OffenseBean
import com.google.gson.annotations.SerializedName

data class OffenseResponse(
    @SerializedName("response")
    val response: OffenseBean
) : BaseResponse()