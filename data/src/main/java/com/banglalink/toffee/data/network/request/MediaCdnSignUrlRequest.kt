package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class MediaCdnSignUrlRequest(
    @SerializedName("customerId")
    val customerId: String? = null,
    @SerializedName("password")
    val password: String? = null,
    @SerializedName("contentId")
    val contentID: String? = null,
    @SerializedName("urlType")
    var urlType: Int = 3,
    @SerializedName("dbVersion")
    var dbVersion: Int = 0,
) : BaseRequest(ApiNames.MEDIA_CDN_SIGN_URL)