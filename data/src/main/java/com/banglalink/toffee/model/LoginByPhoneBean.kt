package com.banglalink.toffee.model

import com.banglalink.toffee.data.network.response.BodyResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginByPhoneBean(
    @SerialName("authorize")
    val authorize: Boolean,
    @SerialName("regSessionToken")
    val regSessionToken: String
) : BodyResponse()