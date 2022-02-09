package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.FeatureContentBean
import com.google.gson.annotations.SerializedName


data class FeatureContentResponse(
    @SerializedName("response")
    val response: FeatureContentBean
) : BaseResponse()