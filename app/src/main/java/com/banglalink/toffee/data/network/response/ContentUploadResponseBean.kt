package com.banglalink.toffee.data.network.response

data class ContentUploadResponseBean(
    val systemTime: String,
    val message: String,
    val contentId: Long,
    val messageType: String
)