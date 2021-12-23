package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class DrmTokenRequest(
    @SerializedName("contentID")
    val contentID: String,
    @SerializedName("userID")
    val userID: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("drmType")
    val drmType: String = "Widevine",
    @SerializedName("licenseDuration")
    val licenseDuration: Int = 0
) : BaseRequest(ApiNames.GET_DRM_TOKEN)