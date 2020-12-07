package com.banglalink.toffee.data.network.request

data class UploadConfirmationRequest(
    val customerId: Int,
    val password: String,
    val contentId: Long,
    val isConfirm: String,
):BaseRequest("ugcContentUploadConfirmation")