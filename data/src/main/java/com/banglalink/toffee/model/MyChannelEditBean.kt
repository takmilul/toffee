package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelEditBean (
    @SerialName("profileImage")
    val profileImage: String? = null,
    @SerialName("bannerImage")
    val bannerImage: String? = null,
    @SerialName("systemTime")
    val systemTime: String? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("messageType")
    val messageType: String? = null
)