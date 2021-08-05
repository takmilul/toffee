package com.banglalink.toffee.data.network.request

data class DrmTokenRequest(
    val contentID: String,
    val userID: Int,
    val drmType:String = "Widevine",
)