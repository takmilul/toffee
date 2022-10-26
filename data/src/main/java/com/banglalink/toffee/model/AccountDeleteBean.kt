package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class AccountDeleteBean(
    @SerializedName("status")
    val status: Boolean?,
    @SerializedName("user_id")
    val user_id: Long?,
    @SerializedName("message")
    val message:String?
)