package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class GrantTokenRequest(
    @SerializedName("app_key")
    var appKey: String? = null,
    @SerializedName("app_secret")
    var appSecret: String? = null
)