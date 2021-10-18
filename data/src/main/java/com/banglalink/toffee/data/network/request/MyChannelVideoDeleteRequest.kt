package com.banglalink.toffee.data.network.request

data class MyChannelVideoDeleteRequest(
    val contentId: Int,
    val customerId: Int,
    val password: String
): BaseRequest("ugcContentDelete")