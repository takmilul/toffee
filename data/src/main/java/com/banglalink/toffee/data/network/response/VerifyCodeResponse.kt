package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.CustomerInfoLogin
import com.google.gson.annotations.SerializedName

data class VerifyCodeResponse(
    @SerializedName("response")
    val customerInfoLogin: CustomerInfoLogin
) : BaseResponse()