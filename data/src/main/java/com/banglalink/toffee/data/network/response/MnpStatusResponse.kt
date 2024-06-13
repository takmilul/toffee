package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MnpStatusResponse(
    @SerialName("response")
    val response: MnpStatusBean? = null
) : BaseResponse()

@Serializable
data class MnpStatusBean (
    @SerialName("mnp_status")
    val mnpStatus  : Int? = null,
    @SerialName("is_bl_number")
    val isBlNumber : Boolean? = null,
    @SerialName("is_prepaid")
    val isPrepaid  : Boolean? = null
)