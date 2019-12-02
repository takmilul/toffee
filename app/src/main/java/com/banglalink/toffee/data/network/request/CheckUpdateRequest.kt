package com.banglalink.toffee.data.network.request

data class CheckUpdateRequest(
    val versionCode: String = "versionCode",
    val os: String = "Android"
) :
    BaseRequest("checkForUpdate")