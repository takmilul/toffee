package com.banglalink.toffee.model

import android.os.Parcelable
import com.banglalink.toffee.data.network.response.BodyResponse
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class CustomerInfoLogin(
    @SerializedName("customerId")
    var customerId: Int = 0,
    @SerializedName("authorize")
    var authorize: Boolean = false,
    @SerializedName("password")
    var password: String? = null,
    @SerializedName("sessionToken")
    var sessionToken: String? = null,
    @SerializedName("profileImage")
    val profileImage: String? = null,
    @SerializedName("systemTime")
    var systemTime: String? = null,
    @SerializedName("balance")
    var balance: Int = 0,
    @SerializedName("dbVersion")
    var dbVersion: DBVersion? = null,
    @SerializedName("dbVersionV2")
    var dbVersionList: List<DBVersionV2>? = null,
    @SerializedName("customerName")
    var customerName: String? = null,
    @SerializedName("referralStatusMessage")
    var referralStatusMessage: String? = null,
    @SerializedName("referralStatus")
    var referralStatus: String? = null,

    @SerializedName("isBanglalinkNumber")
    val isBanglalinkNumber: String? = "false",
    @SerializedName("hlsUrlOverride")
    var hlsUrlOverride: Boolean = false,
    @SerializedName("hlsOverrideUrl")
    var hlsOverrideUrl: String? = null,
    @SerializedName("headerSessionToken")
    var headerSessionToken: String? = null,
    @SerializedName("tokenLifeSpan")
    var tokenLifeSpan: Int = 0,
    @SerializedName("isSubscriptionActive")
    var isSubscriptionActive: String? = "false",

    @SerializedName("real_db_01_url")
    var viewCountDbUrl: String? = null,
    @SerializedName("reaction_db_01_url")
    var reactionDbUrl: String? = null,
    @SerializedName("reaction_db")
    var reactionStatusDbUrl: String? = null,
    @SerializedName("share_log_db")
    var shareCountDbUrl: String? = null,
    @SerializedName("subscribe_count_db")
    var subscriberStatusDbUrl: String? = null,
    @SerializedName("subscribe__db")
    var subscribeDbUrl: String? = null,
    @SerializedName("real_db_01_crc32")
    var viewCountDBVersion: String? = null,
    val isAllTvChannelsMenuEnabled: Boolean = false,
    @SerializedName("geo_city")
    var geoCity: String? = null,
    @SerializedName("geo_location")
    var geoLocation: String? = null,
    @SerializedName("user_ip")
    var userIp: String? = null,
    var isFireworkActive: String? = "false",
    val isFeaturePartnerActive: String? = "false",
    var mqttIsActive: Int = 1,
    var mqttUrl: String? = null,
    @SerializedName("isCastEnable")
    var isCastEnabled: Int = 0,
    @SerializedName("castRecieverId")
    var castReceiverId: String? = null,
    @SerializedName("isCastUrlOverride")
    var isCastUrlOverride: Int = 0,
    @SerializedName("castOverrideUrl")
    var castOverrideUrl: String? = null,
    var verified_status: Boolean = false,
    val internetPackUrl: String? = null,
    val tusUploadServerUrl: String? = null,
    val privacyPolicyUrl: String? = null,
    val creatorsPolicyUrl: String? = null,
    val termsAndConditionsUrl: String? = null,
    val facebookPageUrl: String = "https://www.facebook.com/100869298504557",
    val instagramPageUrl: String = "https://www.instagram.com/toffee.bangladesh/?hl=en",
    val youtubePageUrl: String = "https://www.youtube.com/channel/UCv9NYIjz4jhw-KSqdRulSuw",
    val screenCaptureEnabledUsers: Set<String>? = null,

    @SerializedName("android_in_app_update_version_codes")
    val forceUpdateVersionCodes: String? = null,
    val isVastActive: Int = 0,
    val vastFrequency: Int = 0,
    val gcpVodBucketDirectory: String? = null,
    val isFcmEventActive: Int = 0,
    val isFbEventActive: Int = 0,
    val isGlobalDrmActive: Int = 0,
    val defaultDrmCastReceiver: String? = null,

    @SerializedName("widevineLicenseUrl")
    val widevineLicenseUrl: String? = null,
    val fpsLicenseUrl: String? = null,
    val playreadyLicenseUrl: String? = null,
    val drmTokenUrl: String? = null,
    val isGlobalCidActive: Int = 0,
    val globalCidName: String? = null,
    val androidBetaVersionCode: String? = null,
    @SerializedName("paymentStatus")
    var paymentStatus: Boolean = false,
) : BodyResponse(), Parcelable