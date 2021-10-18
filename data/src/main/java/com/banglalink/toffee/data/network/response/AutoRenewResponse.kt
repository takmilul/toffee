package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

class AutoRenewResponse(
    @SerializedName("response")
    val response:BodyResponse):BaseResponse() {
}