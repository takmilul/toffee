package com.banglalink.toffee.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.banglalink.toffee.data.network.response.BodyResponse
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class CustomerInfoLogin(
    @SerializedName("customerId")
    val customerId: Int = 0,
    @SerializedName("password")
    val password: String? = null,
    @SerializedName("sessionToken")
    val sessionToken: String? = null,
    @SerializedName("profileImage")
    val profileImage: String? = null,
    @SerializedName("systemTime")
    val systemTime: String? = null,
    @SerializedName("balance")
    val balance: Int = 0,
    @SerializedName("dbVersionV2")
    val dbVersionList: List<DBVersionV2>? = null,
    @SerializedName("customerName")
    val customerName: String? = null,
    
    @SerializedName("isBanglalinkNumber")
    val isBanglalinkNumber: String? = "false",
    @SerializedName("hlsUrlOverride")
    val hlsUrlOverride: Boolean = false,
    @SerializedName("hlsOverrideUrl")
    val hlsOverrideUrl: String? = null,
    @SerializedName("headerSessionToken")
    val headerSessionToken: String? = null,
    @SerializedName("tokenLifeSpan")
    val tokenLifeSpan: Int = 0,
    @SerializedName("isSubscriptionActive")
    val isSubscriptionActive: String? = "true",
    
    @SerializedName("real_db_01_url")
    val viewCountDbUrl: String? = null,
    @SerializedName("reaction_db")
    val reactionStatusDbUrl: String? = null,
    @SerializedName("share_log_db")
    val shareCountDbUrl: String? = null,
    @SerializedName("subscribe_count_db")
    val subscriberStatusDbUrl: String? = null,
    @SerializedName("isAllTvChannelsMenuEnabled")
    val isAllTvChannelsMenuEnabled: Boolean = false,
    @SerializedName("geo_city")
    val geoCity: String? = null,
    @SerializedName("geo_region")
    val geoRegion: String? = null,
    @SerializedName("geo_location")
    val geoLocation: String? = null,
    @SerializedName("user_ip")
    val userIp: String? = null,
    @SerializedName("isFeaturePartnerActive")
    val isFeaturePartnerActive: String? = "false",
    @SerializedName("mqttIsActive")
    val mqttIsActive: Int = 0,
    @SerializedName("mqttIsRealtimeSync")
    val isMqttRealtimeSyncActive: Int = 0,
    @SerializedName("mqttUrl")
    val mqttUrl: String? = null,
    @SerializedName("isCastEnable")
    val isCastEnabled: Int = 0,
    @SerializedName("castRecieverId")
    val castReceiverId: String? = null,
    @SerializedName("isCastUrlOverride")
    val isCastUrlOverride: Int = 0,
    @SerializedName("castOverrideUrl")
    val castOverrideUrl: String? = null,
    @SerializedName("verified_status")
    val verified_status: Boolean = false,
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
    val paymentStatus: Boolean = false,
    @SerializedName("isFireworksActiveForAndroid")
    val isFireworkActive: Boolean = false,
    @SerializedName("isStingrayActive")
    val isStingrayActive: Boolean = false,
    @SerializedName("isMedalliaActiveForAndroid")
    val isMedalliaActive: Boolean = false,
    @SerializedName("isConvivaActiveForAndroid")
    val isConvivaActive: Boolean = false,
    @SerializedName("isNdMonitoringActiveAndroid")
    val isPlayerMonitoringActive: Boolean = false,
    @SerializedName("showBuyInternetForAndroid")
    val showBuyInternetForAndroid: Boolean = false,
    @SerializedName("isNativeAdActive")
    val isNativeAdActive: Boolean = false,
    @SerializedName("playerMaxBitRateAndroid")
    val maxBitRateWifi: Int = -1,
    @SerializedName("playerMaxBitRateCellular")
    val maxBitRateCellular: Int = -1,
    @SerializedName("isAppRetrying")
    val isRetryActive: Boolean = true,
    @SerializedName("isAppFailover")
    val isFallbackActive: Boolean = true,
    @SerializedName("appRetryingCount")
    val retryCount: Int = -1,
    @SerializedName("appWaitDuration")
    val retryWaitDuration: Int = -1,
    @SerializedName("videoMinDuration")
    val videoMinDuration: Int = -1,
    @SerializedName("videoMaxDuration")
    val videoMaxDuration: Int = -1,
    @SerializedName("bubbleConfig")
    val bubbleConfig: @RawValue BubbleConfig?,
    @SerializedName("feature_partner_title")
    val featuredPartnerTitle: String? = null,
    @SerializedName("internalTimeout")
    val internalTimeOut: Int? = 60,
    @SerializedName("externalTimeout")
    val externalTimeout: Int? = 60,
    @SerializedName("fStoreTblContentBlacklist")
    val fStoreTblContentBlacklist: String? = null,
    @SerializedName("isfireStoreTblContentBlacklist")
    var isCircuitBreakerActive: Boolean = false,
    @SerializedName("isBubbleActive")
    var isBubbleActive: Int = -1,
    @SerializedName("bubbleType")
    var bubbleType: Int = -1,
    @SerializedName("bubbleDeepLink")
    val ramadanBubbleDeepLink: String? = null,
    @SerializedName("premium_packages")
    var activePackList: @RawValue List<ActivePack>? = null,
    @SerializedName("blDataPackTermsAndConditionsUrl")
    val blDataPackTermsAndConditionsUrl : String? = null,
    @SerializedName("bkashDataPackTermsAndConditionsUrl")
    val bkashDataPackTermsAndConditionsUrl : String? = null,
    @SerializedName("bkashAppKey")
    val bkashAppKey : String? = null,
    @SerializedName("bkashAppSecret")
    val bkashAppSecret : String? = null,
    @SerializedName("bkashPassword")
    val bkashPassword : String? = null,
    @SerializedName("bkashUsername")
    val bkashUsername : String? = null,
    @SerializedName("bkashApiUrl")
    val bkashApiUrl : String? = null,
    @SerializedName("bkashGrantTokenUrl")
    val bkashGrantTokenUrl : String? = null,
    @SerializedName("bkashRefreshTokenUrl")
    val bkashRefreshTokenUrl : String? = null,
    @SerializedName("bkashCreateUrl")
    val bkashCreateUrl : String? = null,
    @SerializedName("bkashExecuteUrl")
    val bkashExecuteUrl : String? = null,
    @SerializedName("bkashQueryPaymentUrl")
    val bkashQueryPaymentUrl : String? = null,
    @SerializedName("bkashCallbackUrl")
    val bkashCallbackUrl : String? = null,
    @SerializedName("merchantInvoiceNumber")
    val merchantInvoiceNumber : String? = null,
    @SerializedName("bkashApiRetryingCount")
    val bkashApiRetryingCount : Int? = 0,
    @SerializedName("bkashApiRetryingDuration")
    val bkashApiRetryingDuration : Long? = 0L,
    @SerializedName("is_prepaid")
    val isPrepaid : Boolean?,
) : BodyResponse(), Parcelable

@Entity
data class BubbleConfig(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("isBubbleActive")
    val isFifaBubbleActive : Boolean,
    @SerializedName("imageType")
    val imageType : String? = null,
    @SerializedName("adIconUrl")
    val adIconUrl : String? = null,
    @SerializedName("bubbleText")
    val bubbleText : String? = null,
    @SerializedName("adForwardUrl")
    val adForwardUrl : String? = null,
    @SerializedName("isGlobalCountDownActive")
    val isGlobalCountDownActive : Boolean,
    @SerializedName("countDownEndTime")
    val countDownEndTime : String? = null,
    @SerializedName("type")
    val type : String? = null,
    @SerializedName("matchStartTime")
    val matchStartTime : String? = null,
    @SerializedName("venue")
    val venue : String? = null,
    @SerializedName("poweredBy")
    val poweredBy : String? = null,
    @SerializedName("poweredByIconUrl")
    val poweredByIconUrl : String? = null,
    @SerializedName("match")
    @Embedded val match : Match? = null,
    @SerializedName("receiveTime")
    val receiveTime: Long = System.currentTimeMillis()
)

data class Match (
    @SerializedName("homeTeam")
    @Embedded val homeTeam : HomeTeam? = null,
    @SerializedName("awayTeam")
    @Embedded val awayTeam : AwayTeam? = null
)

data class HomeTeam (
    @SerializedName("score")
    val homeScore : String? = null,
    @SerializedName("countryName")
    val homeCountryName : String? = null,
    @SerializedName("countryFlag")
    val homeCountryFlag : String? = null
)

data class AwayTeam (
    @SerializedName("score")
    val awayScore : String? = null,
    @SerializedName("countryName")
    val awayCountryName : String? = null,
    @SerializedName("countryFlag")
    val awayCountryFlag : String? = null
)