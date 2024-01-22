package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentShareLogRequest(
    @SerialName("contentId")
    val contentId: Int,
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("sharedUrl")
    val sharedUrl: String? = null,
    @SerialName("contentType")
    val contentType: String = "VOD",
) : BaseRequest(ApiNames.SEND_CONTENT_SHARE_LOG)