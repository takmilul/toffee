package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

class FcmTokenResponse(
    @SerializedName("response")
    val response: FcmTokenBean
) : BaseResponse()