package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogoutBean(
    @SerialName("customerId")
    val customerId:Int,
    @SerialName("password")
    val password:String,
    @SerialName("verified_status")
    val verifyStatus: Boolean
)