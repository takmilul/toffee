package com.banglalink.toffee.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.banglalink.toffee.data.network.response.BodyResponse
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class CustomerInfoLogin(
    @SerialName("customerId")
    val customerId: Int = 0,
    @SerialName("password")
    val password: String? = null,
    @SerialName("sessionToken")
    val sessionToken: String? = null,
    @SerialName("profileImage")
    val profileImage: String? = null,
    @SerialName("systemTime")
    val systemTime: String? = null,
    @SerialName("balance")
    val balance: Int = 0,
    @SerialName("dbVersionV2")
    val dbVersionList: List<DBVersionV2>? = null,
    @SerialName("customerName")
    val customerName: String? = null,
    
//    @SerialName("isBanglalinkNumber")
//    val isBanglalinkNumber: String? = "false",
    @SerialName("hlsUrlOverride")
    val hlsUrlOverride: Boolean = false,
    @SerialName("hlsOverrideUrl")
    val hlsOverrideUrl: String? = null,
    @SerialName("headerSessionToken")
    val headerSessionToken: String? = null,
    @SerialName("tokenLifeSpan")
    val tokenLifeSpan: Int = 0,
    @SerialName("isSubscriptionActive")
    val isSubscriptionActive: String? = "true",
    
    @SerialName("real_db_01_url")
    val viewCountDbUrl: String? = null,
    @SerialName("reaction_db")
    val reactionStatusDbUrl: String? = null,
    @SerialName("share_log_db")
    val shareCountDbUrl: String? = null,
    @SerialName("subscribe_count_db")
    val subscriberStatusDbUrl: String? = null,
    @SerialName("isAllTvChannelsMenuEnabled")
    val isAllTvChannelsMenuEnabled: Boolean = false,
    @SerialName("geo_city")
    val geoCity: String? = null,
    @SerialName("geo_region")
    val geoRegion: String? = null,
    @SerialName("geo_location")
    val geoLocation: String? = null,
    @SerialName("user_ip")
    val userIp: String? = null,
    @SerialName("isFeaturePartnerActive")
    val isFeaturePartnerActive: String? = "false",
    @SerialName("mqttIsActive")
    val mqttIsActive: Int = 0,
    @SerialName("mqttIsRealtimeSync")
    val isMqttRealtimeSyncActive: Int = 0,
    @SerialName("mqttUrl")
    val mqttUrl: String? = null,
    @SerialName("isCastEnable")
    val isCastEnabled: Int = 0,
    @SerialName("castRecieverId")
    val castReceiverId: String? = null,
    @SerialName("isCastUrlOverride")
    val isCastUrlOverride: Int = 0,
    @SerialName("castOverrideUrl")
    val castOverrideUrl: String? = null,
    @SerialName("verified_status")
    val verified_status: Boolean = false,
    @SerialName("internetPackUrl")
    val internetPackUrl: String? = null,
    @SerialName("tusUploadServerUrl")
    val tusUploadServerUrl: String? = null,
    @SerialName("privacyPolicyUrl")
    val privacyPolicyUrl: String? = null,
    @SerialName("creatorsPolicyUrl")
    val creatorsPolicyUrl: String? = null,
    @SerialName("termsAndConditionsUrl")
    val termsAndConditionsUrl: String? = null,
    @SerialName("facebookPageUrl")
    val facebookPageUrl: String = "https://www.facebook.com/100869298504557",
    @SerialName("instagramPageUrl")
    val instagramPageUrl: String = "https://www.instagram.com/toffee.bangladesh/?hl=en",
    @SerialName("youtubePageUrl")
    val youtubePageUrl: String = "https://www.youtube.com/channel/UCv9NYIjz4jhw-KSqdRulSuw",
    @SerialName("screenCaptureEnabledUsers")
    val screenCaptureEnabledUsers: Set<String>? = null,
    
    @SerialName("android_in_app_update_version_codes")
    val forceUpdateVersionCodes: String? = null,
    @SerialName("isVastActive")
    val isVastActive: Int = 0,
    @SerialName("vastFrequency")
    val vastFrequency: Int = 0,
    @SerialName("gcpVodBucketDirectory")
    val gcpVodBucketDirectory: String? = null,
    @SerialName("isFcmEventActive")
    val isFcmEventActive: Int = 0,
    @SerialName("isFbEventActive")
    val isFbEventActive: Int = 0,
    @SerialName("isGlobalDrmActive")
    val isGlobalDrmActive: Int = 0,
    @SerialName("defaultDrmCastReceiver")
    val defaultDrmCastReceiver: String? = null,
    
    @SerialName("widevineLicenseUrl")
    val widevineLicenseUrl: String? = null,
    @SerialName("fpsLicenseUrl")
    val fpsLicenseUrl: String? = null,
    @SerialName("playreadyLicenseUrl")
    val playreadyLicenseUrl: String? = null,
    @SerialName("drmTokenUrl")
    val drmTokenUrl: String? = null,
    @SerialName("isGlobalCidActive")
    val isGlobalCidActive: Int = 0,
    @SerialName("globalCidName")
    val globalCidName: String? = null,
    @SerialName("androidBetaVersionCode")
    val androidBetaVersionCode: String? = null,
    @SerialName("paymentStatus")
    val paymentStatus: Boolean = false,
    @SerialName("isFireworksActiveForAndroid")
    val isFireworkActive: Boolean = false,
    @SerialName("isStingrayActive")
    val isStingrayActive: Boolean = false,
    @SerialName("isMedalliaActiveForAndroid")
    val isMedalliaActive: Boolean = false,
    @SerialName("isConvivaActiveForAndroid")
    val isConvivaActive: Boolean = false,
    @SerialName("isNdMonitoringActiveAndroid")
    val isPlayerMonitoringActive: Boolean = false,
    @SerialName("showBuyInternetForAndroid")
    val showBuyInternetForAndroid: Boolean = false,
    @SerialName("isNativeAdActive")
    val isNativeAdActive: Boolean = false,
    @SerialName("playerMaxBitRateAndroid")
    val maxBitRateWifi: Int = -1,
    @SerialName("playerMaxBitRateCellular")
    val maxBitRateCellular: Int = -1,
    @SerialName("isAppRetrying")
    val isRetryActive: Boolean = true,
    @SerialName("isAppFailover")
    val isFallbackActive: Boolean = true,
    @SerialName("appRetryingCount")
    val retryCount: Int = -1,
    @SerialName("appWaitDuration")
    val retryWaitDuration: Int = -1,
    @SerialName("videoMinDuration")
    val videoMinDuration: Int = -1,
    @SerialName("videoMaxDuration")
    val videoMaxDuration: Int = -1,
    @SerialName("bubbleConfig")
    val bubbleConfig: @RawValue BubbleConfig? = null,
    @SerialName("feature_partner_title")
    val featuredPartnerTitle: String? = null,
    @SerialName("internalTimeout")
    val internalTimeOut: Int? = 60,
    @SerialName("externalTimeout")
    val externalTimeout: Int? = 60,
    @SerialName("fStoreTblContentBlacklist")
    val fStoreTblContentBlacklist: String? = null,
    @SerialName("isfireStoreTblContentBlacklist")
    var isCircuitBreakerActive: Boolean = false,
    @SerialName("isBubbleActive")
    var isBubbleActive: Int = -1,
    @SerialName("bubbleType")
    var bubbleType: Int = -1,
    @SerialName("bubbleDeepLink")
    val ramadanBubbleDeepLink: String? = null,
    @SerialName("premium_packages")
    var activePackList: @RawValue List<ActivePack>? = null,
    @SerialName("blDataPackTermsAndConditionsUrl")
    val blDataPackTermsAndConditionsUrl : String? = null,
    @SerialName("bkashDataPackTermsAndConditionsUrl")
    val bkashDataPackTermsAndConditionsUrl : String? = null,
    @SerialName("bkashAppKey")
    val bkashAppKey : String? = null,
    @SerialName("bkashAppSecret")
    val bkashAppSecret : String? = null,
    @SerialName("bkashPassword")
    val bkashPassword : String? = null,
    @SerialName("bkashUsername")
    val bkashUsername : String? = null,
    @SerialName("bkashApiUrl")
    val bkashApiUrl : String? = null,
    @SerialName("bkashGrantTokenUrl")
    val bkashGrantTokenUrl : String? = null,
    @SerialName("bkashRefreshTokenUrl")
    val bkashRefreshTokenUrl : String? = null,
    @SerialName("bkashCreateUrl")
    val bkashCreateUrl : String? = null,
    @SerialName("bkashExecuteUrl")
    val bkashExecuteUrl : String? = null,
    @SerialName("bkashQueryPaymentUrl")
    val bkashQueryPaymentUrl : String? = null,
    @SerialName("bkashCallbackUrl")
    val bkashCallbackUrl : String? = null,
    @SerialName("merchantInvoiceNumber")
    val merchantInvoiceNumber : String? = null,
    @SerialName("bkashApiRetryingCount")
    val bkashApiRetryingCount : Int? = 0,
    @SerialName("bkashApiRetryingDuration")
    val bkashApiRetryingDuration : Long? = 0L,
    @SerialName("is_prepaid")
    val isPrepaid : Boolean? = true,
    @SerialName("isMnpCallForSubscription")
    val isMnpCallForSubscription : Boolean? = false,
    @SerialName("faqUrl")
    val faqUrl : String? = null,
    @SerialName("isQrCodeEnable")
    val isQrCodeEnable: Int = 0,
    @SerialName("bubblePermissionDialogTitle")
    val bubblePermissionDialogTitle: String? = null,
    @SerialName("bubblePermissionDialogBody")
    val bubblePermissionDialogBody: String? = null,
    @SerialName("bubbleMenuText")
    val bubbleMenuText: String? = null,
) : BodyResponse(), Parcelable

@Entity
@Serializable
data class BubbleConfig(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id")
    val id: Long? = null,
    @SerialName("isBubbleActive")
    val isFifaBubbleActive : Boolean,
    @SerialName("imageType")
    val imageType : String? = null,
    @SerialName("adIconUrl")
    val adIconUrl : String? = null,
    @SerialName("bubbleText")
    val bubbleText : String? = null,
    @SerialName("adForwardUrl")
    val adForwardUrl : String? = null,
    @SerialName("isGlobalCountDownActive")
    val isGlobalCountDownActive : Boolean,
    @SerialName("countDownEndTime")
    val countDownEndTime : String? = null,
    @SerialName("type")
    val type : String? = null,
    @SerialName("matchStartTime")
    val matchStartTime : String? = null,
    @SerialName("venue")
    val venue : String? = null,
    @SerialName("poweredBy")
    val poweredBy : String? = null,
    @SerialName("poweredByIconUrl")
    val poweredByIconUrl : String? = null,
    @SerialName("match")
    @Embedded val match : Match? = null,
    @SerialName("receiveTime")
    val receiveTime: Long = System.currentTimeMillis()
)

@Serializable
data class Match (
    @SerialName("homeTeam")
    @Embedded val homeTeam : HomeTeam? = null,
    @SerialName("awayTeam")
    @Embedded val awayTeam : AwayTeam? = null
)

@Serializable
data class HomeTeam (
    @SerialName("score")
    val homeScore : String? = null,
    @SerialName("countryName")
    val homeCountryName : String? = null,
    @SerialName("countryFlag")
    val homeCountryFlag : String? = null
)

@Serializable
data class AwayTeam (
    @SerialName("score")
    val awayScore : String? = null,
    @SerialName("countryName")
    val awayCountryName : String? = null,
    @SerialName("countryFlag")
    val awayCountryFlag : String? = null
)