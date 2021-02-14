package com.banglalink.toffee.data.network.request

data class ContentShareLogRequest(
    val contentId: Int,
    val customerId: Int,
    val password: String,
    val sharedUrl: String? = null,
    val contentType: String = "VOD",
) : BaseRequest("contentShareLog")