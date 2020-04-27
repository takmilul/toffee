package com.banglalink.toffee.model

import com.banglalink.toffee.data.network.response.BodyResponse
import com.google.gson.annotations.SerializedName

class CustomerInfoSignIn:BodyResponse() {

    @SerializedName("customerId")
    var customerId: Int = 0
    @SerializedName("authorize")
    var authorize: Boolean = false
    @SerializedName("password")
    var password: String? = null
    @SerializedName("sessionToken")
    var sessionToken: String? = null
    @SerializedName("systemTime")
    var systemTime: String? = null
    @SerializedName("balance")
    var balance: Int = 0
    @SerializedName("dbVersion")
    var dbVersion: DBVersion? = null
    @SerializedName("customerName")
    var customerName: String? = null
    @SerializedName("referralStatusMessage")
    var referralStatusMessage: String? = null
    @SerializedName("referralStatus")
    var referralStatus: String? = null

    @SerializedName("isBanglalinkNumber")
    val isBanglalinkNumber:String? = "false"
    @SerializedName("hlsUrlOverride")
    var hlsUrlOverride = false
    @SerializedName("hlsOverrideUrl")
    var hlsOverrideUrl: String? = null
    @SerializedName("headerSessionToken")
    var headerSessionToken: String? = null
    @SerializedName("tokenLifeSpan")
    var tokenLifeSpan = 0
    @SerializedName("isSubscriptionActive")
    var isSubscriptionActive:String? = "true"
}