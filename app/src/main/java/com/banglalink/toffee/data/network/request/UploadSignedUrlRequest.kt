package com.banglalink.toffee.data.network.request

data class UploadSignedUrlRequest (
    val customerId:Int,
    val password: String,
    val objectName: String
) : BaseRequest("uploadCloudStorageSigningUrl")