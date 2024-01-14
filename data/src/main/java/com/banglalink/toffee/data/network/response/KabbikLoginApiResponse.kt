package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class KabbikLoginApiResponse(
    @SerializedName("success" ) var success : String? = null,
    @SerializedName("token"   ) var token   : String? = null,
    @SerializedName("expiry"  ) var expiry  : String? = null
): ExternalBaseResponse()