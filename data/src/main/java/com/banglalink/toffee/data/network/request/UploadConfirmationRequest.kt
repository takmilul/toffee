package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class UploadConfirmationRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("contentId")
    val contentId: Long,
    @SerializedName("isConfirm")
    val isConfirm: String,
    @SerializedName("isCopyrightUploaded")
    val isCopyrightUploaded: Int
) : BaseRequest("ugcContentUploadConfirmation")