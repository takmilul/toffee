package com.banglalink.toffee.data.network.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KabbikLoginApiRequest(
    @SerialName("subscriber_id")
    val subscriberId: String? = null,

    @SerialName("client_id")
    val clientId: String? = null,

    @SerialName("client_secret")
    val clientSecret: String? = null
)