package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

class HeartBeatResponse(
    @SerializedName("response")
    val response: HeartBeatBean
) : BaseResponse()