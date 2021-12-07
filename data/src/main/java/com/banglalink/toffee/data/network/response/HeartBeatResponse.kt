package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.CustomerInfoLogin
import com.google.gson.annotations.SerializedName

class HeartBeatResponse(
    @SerializedName("response")
    val response: CustomerInfoLogin
) : BaseResponse()