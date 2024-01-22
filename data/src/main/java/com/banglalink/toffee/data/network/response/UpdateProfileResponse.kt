package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ProfileResponseBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileResponse(
    @SerialName("response")
    val response: ProfileResponseBean
):BaseResponse()