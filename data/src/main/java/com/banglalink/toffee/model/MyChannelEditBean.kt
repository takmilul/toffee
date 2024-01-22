package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelEditBean (
    @SerialName("profileImage")
    val profileImage: String?,
    @SerialName("bannerImage")
    val bannerImage: String?,
    @SerialName("systemTime")
    val systemTime: String?,
    @SerialName("message")
    val message: String?,
    @SerialName("messageType")
    val messageType: String?
)