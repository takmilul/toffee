package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadSignedUrl(
    @SerialName("uploadSignedUrl")
    val uploadSignedUrl: String
)