package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class ContentShareLogBean(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("messageType")
    val messageType: String? = null
)