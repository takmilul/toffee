package com.banglalink.toffee.model

import com.banglalink.toffee.data.network.response.BodyResponse
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginByPhoneBean(
    @SerialName("authorize")
    val authorize: Boolean = false,
    @SerialName("regSessionToken")
    val regSessionToken: String? = null,
    @SerialName("userType")
    val userType: String? = null
) : BodyResponse()