package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.RechargeByBkashBean
import com.google.gson.annotations.SerializedName

data class RechargeByBkashResponse(
    @SerializedName("response")
    val response: RechargeByBkashBean?
) : BaseResponse()