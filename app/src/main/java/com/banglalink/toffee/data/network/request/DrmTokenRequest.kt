package com.banglalink.toffee.data.network.request

data class DrmTokenRequest(
    val contentID: String,
    val userID: String,
    val drmType:String = "Widevine",
    val licenseDuration: Int = 0
): BaseRequest("drmToken")