package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FollowCategoryBean(
    @SerialName("categoryId")
    val categoryId: Int = 0,
    @SerialName("isFollowed")
    val isFollowed: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("messageType")
    val messageType: String? = null,
    @SerialName("systemTime")
    val systemTime: String? = null
)