package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class KabbikLoginApiResponse(
    @SerializedName("success" ) val success : String? = null,
    @SerializedName("token"   ) val token   : String? = null,
    @SerializedName("expiry"  ) val expiry  : String? = null,
    @SerializedName("message" ) val message : String? = null
): ExternalBaseResponse()