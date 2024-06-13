package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadConfirmationRequest(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("contentId")
    val contentId: Long,
    @SerialName("isConfirm")
    val isConfirm: String,
    @SerialName("isCopyrightUploaded")
    val isCopyrightUploaded: Int
) : BaseRequest(ApiNames.CONFIRM_CONTENT_UPLOAD)