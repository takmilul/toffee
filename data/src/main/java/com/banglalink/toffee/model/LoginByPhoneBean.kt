package com.banglalink.toffee.model

import com.banglalink.toffee.data.network.response.BodyResponse
import com.google.gson.annotations.SerializedName

data class LoginByPhoneBean(
    @SerializedName("authorize")
    val authorize: Boolean,
    @SerializedName("regSessionToken")
    val regSessionToken: String
) : BodyResponse()