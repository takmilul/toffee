package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriberPhotoBean(
    @SerialName("message")
    val message: String?,
    @SerialName("user_photo")
    val userPhoto: String?
)