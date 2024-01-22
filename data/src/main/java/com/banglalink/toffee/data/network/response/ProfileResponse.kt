package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ProfileBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    @SerialName("response")
    val response: ProfileBean
) : BaseResponse()