package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentUploadResponseBean(
    @SerialName("systemTime")
    val systemTime: String,
    @SerialName("message")
    val message: String,
    @SerialName("contentId")
    val contentId: Long,
    @SerialName("messageType")
    val messageType: String,
    @SerialName("uploadVODSignedUrl")
    val uploadVODSignedUrl: String,
    @SerialName("uploadCopyrightSignedUrl")
    val uploadCopyrightSignedUrl: String? = null
)