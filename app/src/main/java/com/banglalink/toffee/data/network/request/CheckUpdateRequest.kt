package com.banglalink.toffee.data.network.request

data class CheckUpdateRequest(
    val versionCode: String,
    val os: String = "Android"
) :
    BaseRequest("checkForUpdateV2")