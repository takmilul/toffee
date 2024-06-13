package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentUploadResponseBean(
    @SerialName("systemTime")
    val systemTime: String? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("contentId")
    val contentId: Long? = null,
    @SerialName("messageType")
    val messageType: String? = null,
    @SerialName("uploadVODSignedUrl")
    val uploadVODSignedUrl: String? = null,
    @SerialName("uploadCopyrightSignedUrl")
    val uploadCopyrightSignedUrl: String? = null
)