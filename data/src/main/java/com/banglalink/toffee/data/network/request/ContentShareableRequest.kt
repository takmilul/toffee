package com.banglalink.toffee.data.network.request

data class ContentShareableRequest(
    val videoShareUrl: String,
    val customerId: Int,
    val password: String,
    val telcoId: Int = 1
) : BaseRequest("getContentsShareable")