package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class VastTagResponseV2(
    @SerializedName("response")
    val response: VastTagBeanV2
) : BaseResponse()