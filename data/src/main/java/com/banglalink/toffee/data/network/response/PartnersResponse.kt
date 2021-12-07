package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.PartnersBean
import com.google.gson.annotations.SerializedName

data class PartnersResponse(
    @SerializedName("response")
    val response: PartnersBean
) : BaseResponse()