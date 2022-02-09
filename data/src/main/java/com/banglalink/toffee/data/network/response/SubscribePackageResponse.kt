package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

class SubscribePackageResponse(
    @SerializedName("response")
    val response: BodyResponse
) : BaseResponse()