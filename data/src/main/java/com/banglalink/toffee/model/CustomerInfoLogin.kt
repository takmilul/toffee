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
    @SerializedName("isAllTvChannelsMenuEnabled")
    val isAllTvChannelsMenuEnabled: Boolean = false,
    @SerializedName("geo_city")
    var geoCity: String? = null,
    @SerializedName("geo_region")
    var geoRegion: String? = null,
    @SerializedName("geo_location")
    var geoLocation: String? = null,
    @SerializedName("user_ip")
    var userIp: String? = null,
    @SerializedName("isFeaturePartnerActive")
    val isFeaturePartnerActive: String? = "false",
    @SerializedName("mqttIsActive")
    var mqttIsActive: Int = 1,
    @SerializedName("mqttUrl")
    var mqttUrl: String? = null,
    @SerializedName("isCastEnable")
    var isCastEnabled: Int = 0,
    @SerializedName("castRecieverId")
    var castReceiverId: String? = null,
    @SerializedName("isCastUrlOverride")
    var isCastUrlOverride: Int = 0,
    @SerializedName("castOverrideUrl")
    var castOverrideUrl: String? = null,
    @SerializedName("verified_status")
    var verified_status: Boolean = false,
    @SerializedName("internetPackUrl")
    val internetPackUrl: String? = null,
    @SerializedName("tusUploadServerUrl")
    val tusUploadServerUrl: String? = null,
    @SerializedName("privacyPolicyUrl")
    val privacyPolicyUrl: String? = null,
    @SerializedName("creatorsPolicyUrl")
    val creatorsPolicyUrl: String? = null,
    @SerializedName("termsAndConditionsUrl")
    val termsAndConditionsUrl: String? = null,
    @SerializedName("facebookPageUrl")
    val facebookPageUrl: String = "https://www.facebook.com/100869298504557",
    @SerializedName("instagramPageUrl")
    val instagramPageUrl: String = "https://www.instagram.com/toffee.bangladesh/?hl=en",
    @SerializedName("youtubePageUrl")
    val youtubePageUrl: String = "https://www.youtube.com/channel/UCv9NYIjz4jhw-KSqdRulSuw",
    @SerializedName("screenCaptureEnabledUsers")
    val screenCaptureEnabledUsers: Set<String>? = null,
    
    @SerializedName("android_in_app_update_version_codes")
    val forceUpdateVersionCodes: String? = null,
    @SerializedName("isVastActive")
    val isVastActive: Int = 0,
    @SerializedName("vastFrequency")
    val vastFrequency: Int = 0,
    @SerializedName("gcpVodBucketDirectory")
    val gcpVodBucketDirectory: String? = null,
    @SerializedName("isFcmEventActive")
    val isFcmEventActive: Int = 0,
    @SerializedName("isFbEventActive")
    val isFbEventActive: Int = 0,
    @SerializedName("isGlobalDrmActive")
    val isGlobalDrmActive: Int = 0,
    @SerializedName("defaultDrmCastReceiver")
    val defaultDrmCastReceiver: String? = null,
    
    @SerializedName("widevineLicenseUrl")
    val widevineLicenseUrl: String? = null,
    @SerializedName("fpsLicenseUrl")
    val fpsLicenseUrl: String? = null,
    @SerializedName("playreadyLicenseUrl")
    val playreadyLicenseUrl: String? = null,
    @SerializedName("drmTokenUrl")
    val drmTokenUrl: String? = null,
    @SerializedName("isGlobalCidActive")
    val isGlobalCidActive: Int = 0,
    @SerializedName("globalCidName")
    val globalCidName: String? = null,
    @SerializedName("androidBetaVersionCode")
    val androidBetaVersionCode: String? = null,
    @SerializedName("paymentStatus")
    var paymentStatus: Boolean = false,
    @SerializedName("isFireworksActiveForAndroid")
    var isFireworkActive:Boolean = false,
    @SerializedName("isStingrayActive")
    var isStingrayActive:Boolean = false,
    @SerializedName("isMedalliaActiveForAndroid")
    var isMedalliaActive:Boolean = false,
    @SerializedName("isConvivaActiveForAndroid")
    var isConvivaActive:Boolean = false,
    @SerializedName("isNdMonitoringActiveAndroid")
    var isPlayerMonitoringActive: Boolean = false,
    @SerializedName("showBuyInternetForAndroid")
    var showBuyInternetForAndroid: Boolean = false,
    @SerializedName("isNativeAdActive")
    var isNativeAdActive: Boolean = false,
    @SerializedName("playerMaxBitRateAndroid")
    var maxBitRateWifi: Int = -1,
    @SerializedName("playerMaxBitRateCellular")
    var maxBitRateCellular: Int = -1,
    @SerializedName("isAppRetrying")
    var isRetryActive: Boolean = true,
    @SerializedName("isAppFailover")
    var isFallbackActive: Boolean = true,
    @SerializedName("appRetryingCount")
    var retryCount: Int = -1,
    @SerializedName("appWaitDuration")
    var retryWaitDuration: Int = -1,
) : BodyResponse(), Parcelable