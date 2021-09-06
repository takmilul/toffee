package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class ContentUploadResponseBean(
    @SerializedName("systemTime") val systemTime: String,
    @SerializedName("message") val message: String,
    @SerializedName("contentId") val contentId: Long,
    @SerializedName("messageType") val messageType: String,
    @SerializedName("uploadVODSignedUrl") val uploadVODSignedUrl: String,
    @SerializedName("uploadCopyrightSignedUrl") val uploadCopyrightSignedUrl: String? = null
)