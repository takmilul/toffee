package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaCdnSignUrlRequest(
    @SerialName("customerId")
    val customerId: String? = null,
    @SerialName("password")
    val password: String? = null,
    @SerialName("contentId")
    val contentID: String? = null,
    @SerialName("urlType")
    var urlType: Int = 3,
    @SerialName("dbVersion")
    var dbVersion: Int = 0,
) : BaseRequest(ApiNames.MEDIA_CDN_SIGN_URL)