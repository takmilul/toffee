package com.banglalink.toffee.data.network.request


data class ViewingContentRequest(
    val type: String,
    val contentId: Int,
    val customerId: Int,
    val password: String,
    val lat: String,
    val lon: String
):BaseRequest("viewingContent")