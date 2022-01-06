package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class FireworkResponse(
    @SerializedName("response")
    val response: FireworkBean
):BaseResponse()
