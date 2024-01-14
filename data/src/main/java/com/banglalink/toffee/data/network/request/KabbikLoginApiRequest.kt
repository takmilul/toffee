package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName
import retrofit2.http.Header

data class KabbikLoginApiRequest(
    @SerializedName("subscriber_id")
    val subscriberId: String? = null,

    @SerializedName("client_id")
    val clientId: String? = null,

    @SerializedName("client_secret")
    val clientSecret: String? = null
)