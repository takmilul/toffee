package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

class MediaCdnSignUrl {
    @SerializedName("balance")
    val balance:Int = 0
    @SerializedName("systemTime")
    val systemTime: String? = null
    @SerializedName("serverTime")
    val serverTime: String? = null
    @SerializedName("content_id")
    val contentId:Int = 0
    @SerializedName("sign_url")
    val signUrl: String? = null
    @SerializedName("sign_url_expire")
    var signedUrlExpiryDate: String? =null
//    @SerializedName("sign_cookie")
//    var signCookie: String? = null
//    @SerializedName("sign_cookie_expire")
//    var signCookieDate: String? = null
}