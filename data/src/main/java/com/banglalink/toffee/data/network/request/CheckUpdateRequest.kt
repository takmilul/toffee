package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class CheckUpdateRequest(
    @SerializedName("versionCode")
    val versionCode: String,
    @SerializedName("os")
    val os: String = "Android"
) : BaseRequest("checkForUpdateV2")