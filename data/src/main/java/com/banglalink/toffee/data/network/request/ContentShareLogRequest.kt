package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class ContentShareLogRequest(
    @SerializedName("contentId")
    val contentId: Int,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("sharedUrl")
    val sharedUrl: String? = null,
    @SerializedName("contentType")
    val contentType: String = "VOD",
) : BaseRequest(ApiNames.SEND_CONTENT_SHARE_LOG)