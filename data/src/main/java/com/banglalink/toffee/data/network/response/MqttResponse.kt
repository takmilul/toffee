package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class MqttResponse(
    @SerializedName("response")
    val response: MqttBean?
) : BaseResponse()