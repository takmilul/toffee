package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.Credential
import com.google.gson.annotations.SerializedName

data class CredentialResponse (
    @SerializedName("response")
    val credential: Credential?)
    :BaseResponse()