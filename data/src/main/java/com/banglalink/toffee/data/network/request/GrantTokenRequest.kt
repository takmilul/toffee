package com.banglalink.toffee.data.network.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GrantTokenRequest(
    @SerialName("app_key")
    var appKey: String? = null,
    @SerialName("app_secret")
    var appSecret: String? = null
)