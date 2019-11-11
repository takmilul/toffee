package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.CustomerInfoSignIn
import com.google.gson.annotations.SerializedName

data class ApiLoginResponse(
    @SerializedName("response")
    val customerInfoSignIn: CustomerInfoSignIn?)
    :BaseResponse()