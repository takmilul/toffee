package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.Customer
import com.banglalink.toffee.model.ProfileBean
import com.google.gson.annotations.SerializedName

data class PairWithTvResponse(
    @SerializedName("response")
    val response: PairStatus
) : BaseResponse()


data class PairStatus(
    @SerializedName("status")
    val status: Int // 0 = wrong code, 1 = active, 2 = expired
)