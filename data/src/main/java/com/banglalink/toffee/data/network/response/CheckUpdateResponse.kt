package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.CheckUpdateBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckUpdateResponse(
    @SerialName("response")
    val response: CheckUpdateBean? = null
) : BaseResponse()