package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountDeleteBean(
    @SerialName("status")
    val status: Boolean? = false,
    @SerialName("user_id")
    val user_id: Long? = 0,
    @SerialName("message")
    val message:String? = null
)