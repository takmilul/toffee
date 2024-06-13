package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.Credential
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CredentialResponse(
    @SerialName("response")
    val credential: Credential? = null
) : BaseResponse()