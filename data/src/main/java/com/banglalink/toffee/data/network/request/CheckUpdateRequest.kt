package com.banglalink.toffee.data.network.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckUpdateRequest(
    @SerialName("versionCode")
    val versionCode: String,
    @SerialName("os")
    val os: String = "Android"
) : BaseRequest("checkForUpdateV2")