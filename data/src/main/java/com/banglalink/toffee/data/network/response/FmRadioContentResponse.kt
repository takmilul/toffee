package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.FmRadioContentBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FmRadioContentResponse (
    @SerialName("response")
    val response: FmRadioContentBean? = null
) : BaseResponse()