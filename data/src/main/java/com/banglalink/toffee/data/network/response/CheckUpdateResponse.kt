package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.CheckUpdateBean
import com.google.gson.annotations.SerializedName

data class CheckUpdateResponse(
    @SerializedName("response")
    val response: CheckUpdateBean
) : BaseResponse()