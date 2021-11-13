package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class CheckUpdateBean(
    @SerializedName("updateAvailable")
    val updateAvailable: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("messageTitle")
    var messageTitle: String) {
}