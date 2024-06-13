package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class MediaCdnSignUrl {
    @SerialName("balance")
    val balance:Int = 0
    @SerialName("systemTime")
    val systemTime: String? = null
    @SerialName("serverTime")
    val serverTime: String? = null
    @SerialName("content_id")
    val contentId:Int = 0
    @SerialName("sign_url")
    val signedUrl: String? = null
    @SerialName("sign_url_expire")
    var signedUrlExpiryDate: String? = null
    @SerialName("sign_cookie")
    var signedCookie: String? = null
    @SerialName("sign_cookie_expire")
    var signedCookieExpiryDate: String? = null
}