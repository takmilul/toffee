package com.banglalink.toffee.model

import android.os.Parcelable
import android.text.Spanned
import android.util.Base64
import androidx.core.text.HtmlCompat
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.util.Utils
import com.google.android.gms.common.annotation.KeepName
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.*

@KeepName
@Parcelize
data class ChannelInfo(
    @SerializedName("id")
    var id: String,
    @SerializedName("main_table_id")
    var mainTableId: String? = "0",
    @SerializedName("iptv_programs_id")
    var iptvProgramsId: String? = "0",
    @SerializedName("program_name")
    var program_name: String? = null,
    @SerializedName("video_share_url")
    var video_share_url: String? = null,
    @SerializedName("video_trailer_url")
    var video_trailer_url: String? = null,
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("water_mark_url")
    var water_mark_url: String? = null,
    @SerializedName("type")
    var type: String? = null,
    @SerializedName("view_count")
    var view_count: String? = null,
    @SerializedName("lcn")
    var lcn: String? = null,
    @SerializedName("individual_price")
    var individual_price: String? = null,
    @SerializedName("video_tags")
    var video_tags: String? = null,
    @SerializedName("duration")
    var duration: String? = null,
    @SerializedName("age_restriction")
    var age_restriction: String? = null,
    @SerializedName("service_operator_id")
    var service_operator_id: String? = null,
    @SerializedName("logo_mobile_url")
    var logo_mobile_url: String? = null,
    @SerializedName("poster_url_mobile")
    var poster_url_mobile: String? = null,
    @SerializedName("subscription")
    var subscription:Boolean = false,
    @SerializedName("individual_purchase")
    var individual_purchase: Boolean = false,
    @SerializedName("expireTime")
    var expireTime: String? = null,
    @SerializedName("hlsLinks")
    var hlsLinks: List<HlsLinks>? = null,
    
    @SerializedName("drm_dash_url_extended")
    var drmDashUrlExt: List<DrmHlsLinks>? = null,
    @SerializedName("drm_dash_url_extended_sd")
    var drmDashUrlExtSd: List<DrmHlsLinks>? = null,
    @SerializedName("drm_dash_url_sd")
    var drmDashUrlSd: String? = null,
    @SerializedName("content_expire")
    var contentExpiryTime: String? = null,
    
    @SerializedName("channel_logo")
    var channel_logo: String? = null,
    @SerializedName("categoryName")
    var category: String? = null,
    @SerializedName("subCategory")
    var subCategory: String? = null,
    @SerializedName("categoryId")
    var categoryId: Int = 0,
    @SerializedName("subCategoryId")
    var subCategoryId: Int = 0,
    @SerializedName("favorite")
    var favorite: String? = null,
    @SerializedName("potrait_ratio_800_1200")
    var portrait_ratio_800_1200: String? = null,
    @SerializedName("landscape_ratio_1280_720")
    var landscape_ratio_1280_720: String? = null,
    @SerializedName("feature_image")
    var feature_image: String? = null,
    @SerializedName("content_provider_name")
    var content_provider_name: String? = null,
    @SerializedName("content_provider_id")
    var content_provider_id: String? = null,
    @SerializedName("channel_owner_id")
    val channel_owner_id: Int = 0,
    @SerializedName("isSubscribed")
    var isSubscribed: Int = 0,
    @SerializedName("subscriberCount")
    var subscriberCount: Long = 0,
    
    @SerializedName("serial_name")
    val seriesName: String? = null,
    @SerializedName("total_season_no")
    val totalSeason: Int = 0,
    @SerializedName("season_no")
    val seasonNo: Int = 0,
    @SerializedName("serial_summary_id")
    val seriesSummaryId: Int = 0,
    @SerializedName("total_episode_no")
    val totalEpisode: Int = 0,
    @SerializedName("episode_no")
    val episodeNo: Int = 0,
    
    @SerializedName("is_available")
    var is_available: Int = 0,
    @SerializedName("reaction")
    var reaction: ReactionStatus? = null,   //individual reaction count from server
    @SerializedName("myReaction")
    var myReaction: Int = Reaction.None.value, //enum value (Reaction.Like.value) etc...
    @SerializedName("shareCount")
    var shareCount: Long = 0L,
    @SerializedName("playlist_content_id")
    val playlistContentId: Int = 0,
    @SerializedName("active_season_list")
    var activeSeasonList: List<Int>? = listOf(1),
    @SerializedName("channel_profile_url")
    val channelProfileUrl: String? = null,
    @SerializedName("url_type")
    val urlType: Int = 0,
    @SerializedName("url_type_extended")
    val urlTypeExt: Int = 0,
    @SerializedName("is_approved")
    val is_approved: Int? = null,
    @SerializedName("created_at")
    val created_at: String? = null,
    @SerializedName("is_horizontal")
    val is_horizontal: Int? = null,
    @SerializedName("landscape_feature_1280_720")
    val ugcFeaturedImage: String? = null,
    @SerializedName("is_encoded")
    val isEncoded: Int? = null,
    @SerializedName("is_ugc")
    val is_ugc: Int = 0,
    
    @SerializedName("is_drm_active")
    var is_drm_active: Int = 0,
    @SerializedName("drm_dash_url")
    val drmDashUrl: String? = null,
    @SerializedName("drm_cast_receiver")
    val drmCastReceiver: String? = null,
    @SerializedName("plain_cast_receiver")
    val plainCastReceiver: String? = null,
    @SerializedName("is_ad_active")
    val is_ad_active: Int = 0,
    @SerializedName("drm_cid")
    val drmCid: String? = null,
    @SerializedName("fcm_event_name")
    val fcmEventName: String? = null,
    @SerializedName("fcm_event_is_active")
    val fcm_event_is_active: Int = 0,
    @SerializedName("data_source")
    val dataSource: String? = "iptv_programs",
    @SerializedName("totalCount")
    var totalCount: Int = 0,
    @SerializedName("plain_hls_url_for_url_type")
    var paidPlainHlsUrl: String? = null,
    @SerializedName("sign_url_expire")
    var signedUrlExpiryDate: String? =null,
    @SerializedName("cdn_type")
    var cdnType: String? = null,
    @SerializedName("ads_group")
    var adGroup: String? = null,
    @SerializedName("featured_banner_code")
    var bannerEventName: String? = null,
    @SerializedName("sign_cookie")
    var signedCookie: String? = null,
    @SerializedName("sign_cookie_expire")
    var signedCookieExpiryDate: String? =null,
) :Parcelable {
    
    @get:SerializedName("isApproved")
    val isApproved: Int
        get() = if (is_approved == null || is_approved == 1) 1 else 0
    
    @get:SerializedName("isHorizontal")
    val isHorizontal: Int
        get() = if (is_horizontal == null || is_horizontal == 1) 1 else 0

    @get:SerializedName("isLive")
    val isLive: Boolean
        get() = "LIVE".equals(type, ignoreCase = true)
    
    @get:SerializedName("isLinear")
    val isLinear: Boolean
        get() = "LIVE".equals(type, ignoreCase = true) || "Stingray".equals(type, ignoreCase = true) || "RADIO".equals(type, ignoreCase = true)
    
    @get:SerializedName("isVOD")
    val isVOD: Boolean
        get() = "VOD".equals(type, ignoreCase = true)

    @get:SerializedName("isChannel")
    val isChannel: Boolean
        get() = "CHANNEL".equals(type, ignoreCase = true)

    @get:SerializedName("isStingray")
    val isStingray: Boolean
        get() = "stingray".equals(type, ignoreCase = true)

    @get:SerializedName("isFmRadio")
    val isFmRadio: Boolean
        get() = "RADIO".equals(type, ignoreCase = true)
    
    @get:SerializedName("isCatchup")
    val isCatchup: Boolean
        get() = "CATCHUP".equals(type, ignoreCase = true)
    
    @get:SerializedName("isBucketUrl")
    val isBucketUrl: Boolean
        get() = isEncoded == 0
    
    @get:SerializedName("isDrmActive")
    val isDrmActive: Boolean
        get() = is_drm_active == 1
    
    @get:SerializedName("isAdActive")
    val isAdActive: Boolean
        get() = is_ad_active == 1
    
    @get:SerializedName("isFcmEventActive")
    val isFcmEventActive: Boolean
        get() = fcm_event_is_active == 1
    
    @IgnoredOnParcel
    @SerializedName("isExpired")
    var isExpired: Boolean = false
    
    @IgnoredOnParcel
    @SerializedName("isOwner")
    var isOwner: Boolean = false
    
    @IgnoredOnParcel
    @SerializedName("isPublic")
    var isPublic: Boolean = false
    
    @IgnoredOnParcel
    @SerializedName("isPlaylist")
    var isPlaylist: Boolean = false
    
    @IgnoredOnParcel
    @SerializedName("isFromSportsCategory")
    var isFromSportsCategory: Boolean = false
    
    @IgnoredOnParcel
    @SerializedName("viewProgress")
    var viewProgress: Long = -1L
    
    fun getContentId(): String {
        return if (isOwner && !isPublic && !isPlaylist && mainTableId != null && mainTableId != "0"){
            mainTableId ?: id
        } else if (isOwner && !isPublic && isPlaylist && iptvProgramsId != null && iptvProgramsId != "0") {
            iptvProgramsId ?: id
        } else {
            id
        }
    }
    
    fun getCategory(category: String, subCategory: String): String {
        var itemCategory = "Channels>$category"
        if (subCategory.isNotEmpty()) {
            itemCategory += ">$subCategory"
        }
        return itemCategory
    }
    
    fun viewProgressPercent(): Int {
        val durationInt = Utils.getLongDuration(duration)
        if(viewProgress < 0L || durationInt <= 0L || isLive) return 0
        if(viewProgress > durationInt) return 1000
        return ((viewProgress.toDouble() / durationInt) * 1000L).toInt()
    }
    
    fun getDescriptionDecoded(): Spanned? {
        return try {
//            val obtainedUrls: MutableList<String> = ArrayList()
            var descriptionDecoded = String(Base64.decode(description, Base64.NO_WRAP))
//            val pattern = Pattern.compile(Patterns.WEB_URL.toString())
//            val matcher = pattern.matcher(descriptionDecoded)
//            while (matcher.find()) {
//                obtainedUrls.add(matcher.group())
//            }
//
//            for (obtainedUrl in obtainedUrls) {
//                val lowerCaseUrl = obtainedUrl.lowercase()
//                descriptionDecoded = descriptionDecoded.replace(obtainedUrl, lowerCaseUrl)
//            }
            HtmlCompat.fromHtml(descriptionDecoded
                .trim()
                .replace("\n", "<br/>"),
                HtmlCompat.FROM_HTML_MODE_LEGACY)
        } catch (ex: Exception) {
            null
        }
    }
    fun isContentUrlExpired(serverDate:Date):Boolean{
        return try {
            serverDate.after(Utils.getDate(signedUrlExpiryDate))
        } catch (ne: NullPointerException) {
            true
        }
    }
    fun getHlsLink(): String? = hlsLinks?.get(0)?.hls_url_mobile
    
    fun getDrmUrl(isDataConnection: Boolean) = if (isDataConnection) {
        drmDashUrlExtSd?.firstOrNull()?.urlList()?.randomOrNull()?.takeIf { it.isNotBlank() } ?: drmDashUrlExt?.firstOrNull()?.urlList()?.randomOrNull()?.takeIf { it.isNotBlank() } ?: drmDashUrl?.takeIf { it.isNotBlank() }
    } else {
        drmDashUrlExt?.firstOrNull()?.urlList()?.randomOrNull()?.takeIf { it.isNotBlank() } ?: drmDashUrlExtSd?.firstOrNull()?.urlList()?.randomOrNull()?.takeIf { it.isNotBlank() } ?: drmDashUrl?.takeIf { it.isNotBlank() }
    }

    fun formattedShareCount(): String = Utils.getFormattedViewsText(shareCount.toString())

    @get:SerializedName("isPurchased")
    val isPurchased: Boolean
        get() = (individual_price?.toInt() ?: 0) > 0 && individual_purchase
    
    @get:SerializedName("isPaidSubscribed")
    val isPaidSubscribed: Boolean
        get() = individual_price?.toInt() == 0 && subscription

    fun isExpired(serverDate: Date): Boolean {
        return try {
            serverDate.after(Utils.getDate(expireTime))
        } catch (ne: NullPointerException) {
            true
        }
    }
    
    fun durationInSeconds(): Int {
        val time = try {
            val format = SimpleDateFormat("HH:mm:ss", Locale.US)
            format.parse(duration ?: "00:00:00")?.seconds ?: 0
        } catch (e: Exception) {
            0
        }
        return time
    }
    
    fun formattedViewCount(): String = Utils.getFormattedViewsText(view_count)
    fun formattedDuration(): String = Utils.discardZeroFromDuration(duration)
    fun formattedCreateTime(): String = if(!created_at.isNullOrBlank()) Utils.getDateDiffInDayOrHourOrMinute(Utils.getDate(created_at).time) else "0"
    fun formattedSubscriberCount(): String = Utils.getFormattedViewsText(subscriberCount.toString())
}