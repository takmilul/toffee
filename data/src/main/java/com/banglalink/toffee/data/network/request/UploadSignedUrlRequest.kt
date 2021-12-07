package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class UploadSignedUrlRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("objectName")
    val objectName: String
) : BaseRequest("uploadCloudStorageSigningUrl")