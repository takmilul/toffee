package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.PartnersBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PartnersResponse(
    @SerialName("response")
    val response: PartnersBean
) : BaseResponse()