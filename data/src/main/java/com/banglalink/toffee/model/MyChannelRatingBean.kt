package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelRatingBean (
    @SerialName("isRated")
    val isRated: Int = 0,
    @SerialName("ratingCount")
    val ratingCount: Float = 0f,
    @SerialName("message")
    val message: String? = null,
    @SerialName("messageType")
    val messageType: String? = null,
    @SerialName("systemTime")
    val systemTime: String? = null
)