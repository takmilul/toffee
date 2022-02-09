package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class ProfileResponseBean(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String?
)