package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadSignedUrlRequest(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("objectName")
    val objectName: String
) : BaseRequest(ApiNames.UPLOAD_SIGNED_URL)