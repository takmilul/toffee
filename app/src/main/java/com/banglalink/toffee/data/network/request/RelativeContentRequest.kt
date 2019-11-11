package com.banglalink.toffee.data.network.request

data class RelativeContentRequest(
    val contentId: String,
    val videoTag: String,
    val customerId: Int,
    val password: String,
    val offset: Int,
    val limit: Int
):BaseRequest("getRelativeContentsExt")