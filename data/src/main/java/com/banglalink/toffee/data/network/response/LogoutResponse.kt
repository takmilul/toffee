package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.LogoutBean
import com.google.gson.annotations.SerializedName

data class LogoutResponse(
    @SerializedName("response")
    val response: LogoutBean
) : BaseResponse()