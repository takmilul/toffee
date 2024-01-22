package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.FeatureContentBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeatureContentResponse(
    @SerialName("response")
    val response: FeatureContentBean
) : BaseResponse()