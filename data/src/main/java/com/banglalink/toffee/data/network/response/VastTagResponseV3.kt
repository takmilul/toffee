package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class VastTagResponseV3(
    @SerializedName("response")
    val response: VastTagBeanV3? = null
) : BaseResponse()