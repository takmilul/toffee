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
    @SerializedName("dbVersionV2")
    var dbVersionList: List<DBVersionV2>? = null
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

    @SerializedName("real_db_01_url")
    var viewCountDbUrl:String? = null
    @SerializedName("reaction_db_01_url")
    var reactionDbUrl:String? = null
    @SerializedName("reaction_db")
    var reactionStatusDbUrl:String? = null
    @SerializedName("share_log_db")
    var shareLogDb: String? = null
    @SerializedName("subscribe_count_db")
    var subscriberStatusDbUrl: String? = null
    @SerializedName("subscribe__db")
    var subscribeDbUrl: String? = null
    @SerializedName("real_db_01_crc32")
    var viewCountDBVersion:String? = null
}