package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountDeleteBean(
    @SerialName("status")
    val status: Boolean?,
    @SerialName("user_id")
    val user_id: Long?,
    @SerialName("message")
    val message:String?
)