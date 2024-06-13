package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KabbikLoginApiResponse(
    @SerialName("success" ) val success : String? = null,
    @SerialName("token"   ) val token   : String? = null,
    @SerialName("expiry"  ) val expiry  : String? = null,
    @SerialName("message" ) val message : String? = null
): ExternalBaseResponse()