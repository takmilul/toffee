package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VastTagResponseV3(
    @SerialName("response")
    val response: VastTagBeanV3? = null
) : BaseResponse()