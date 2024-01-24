package com.banglalink.toffee.model

import android.os.Parcelable
import android.text.Spanned
import android.util.Base64
import androidx.core.text.HtmlCompat
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.util.Utils
import com.google.android.gms.common.annotation.KeepName
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@KeepName
@Parcelize
@Serializable
data class ChannelInfo(
    @SerialName("id")
    var id: String = "0",
    @SerialName("main_table_id")
    var mainTableId: String? = "0",
    @SerialName("iptv_programs_id")
    var iptvProgramsId: String? = "0",
    @SerialName("program_name")
    var program_name: String? = null,
    @SerialName("video_share_url")
    var video_share_url: String? = null,
    @SerialName("video_trailer_url")
    var video_trailer_url: String? = null,
    @SerialName("description")
    var description: String? = null,
    @SerialName("water_mark_url")
    var water_mark_url: String? = null,
    @SerialName("type")
    var type: String? = null,
    @SerialName("view_count")
    var view_count: String? = null,
    @SerialName("lcn")
    var lcn: String? = null,
    @SerialName("individual_price")
    var individual_price: String? = null,
    @SerialName("video_tags")
    var video_tags: String? = null,
    @SerialName("duration")
    var duration: String? = null,
    @SerialName("age_restriction")
    var age_restriction: String? = null,
    @SerialName("service_operator_id")
    var service_operator_id: String? = null,
    @SerialName("logo_mobile_url")
    var logo_mobile_url: String? = null,
    @SerialName("poster_url_mobile")
    var poster_url_mobile: String? = null,
    @SerialName("subscription")
    var subscription:Boolean = false,
    @SerialName("individual_purchase")
    var individual_purchase: Boolean = false,
    @SerialName("expireTime")
    var expireTime: String? = null,
    @SerialName("hlsLinks")
    var hlsLinks: List<HlsLinks>? = null,
    
    @SerialName("drm_dash_url_extended")
    var drmDashUrlExt: List<DrmHlsLinks>? = null,
    @SerialName("drm_dash_url_extended_sd")
    var drmDashUrlExtSd: List<DrmHlsLinks>? = null,
    @SerialName("drm_dash_url_sd")
    var drmDashUrlSd: String? = null,
    @SerialName("content_expire")
    var contentExpiryTime: String? = null,
    
    @SerialName("channel_logo")
    var channel_logo: String? = null,
    @SerialName("categoryName")
    var category: String? = null,
    @SerialName("subCategory")
    var subCategory: String? = null,
    @SerialName("categoryId")
    var categoryId: Int = 0,
    @SerialName("subCategoryId")
    var subCategoryId: Int = 0,
    @SerialName("favorite")
    var favorite: String? = null,
    @SerialName("potrait_ratio_800_1200")
    var portrait_ratio_800_1200: String? = null,
    @SerialName("landscape_ratio_1280_720")
    var landscape_ratio_1280_720: String? = null,
    @SerialName("feature_image")
    var feature_image: String? = null,
    @SerialName("content_provider_name")
    var content_provider_name: String? = null,
    @SerialName("content_provider_id")
    var content_provider_id: String? = null,
    @SerialName("channel_owner_id")
    val channel_owner_id: Int = 0,
    @SerialName("isSubscribed")
    var isSubscribed: Int = 0,
    @SerialName("subscriberCount")
    var subscriberCount: Long = 0,
    
    @SerialName("serial_name")
    val seriesName: String? = null,
    @SerialName("total_season_no")
    val totalSeason: Int = 0,
    @SerialName("season_no")
    val seasonNo: Int = 0,
    @SerialName("serial_summary_id")
    val seriesSummaryId: Int = 0,
    @SerialName("total_episode_no")
    val totalEpisode: Int = 0,
    @SerialName("episode_no")
    val episodeNo: Int = 0,
    
    @SerialName("is_available")
    var is_available: Int = 0,
    @SerialName("reaction")
    var reaction: ReactionStatus? = null,   //individual reaction count from server
    @SerialName("myReaction")
    var myReaction: Int = Reaction.None.value, //enum value (Reaction.Like.value) etc...
    @SerialName("shareCount")
    var shareCount: Long = 0L,
    @SerialName("playlist_content_id")
    val playlistContentId: Int = 0,
    @SerialName("active_season_list")
    var activeSeasonList: List<Int>? = listOf(1),
    @SerialName("channel_profile_url")
    val channelProfileUrl: String? = null,
    @SerialName("url_type")
    val urlType: Int = 0,
    @SerialName("url_type_extended")
    val urlTypeExt: Int = 0,
    @SerialName("is_approved")
    val is_approved: Int? = null,
    @SerialName("created_at")
    val created_at: String? = null,
    @SerialName("is_horizontal")
    val is_horizontal: Int? = null,
    @SerialName("landscape_feature_1280_720")
    val ugcFeaturedImage: String? = null,
    @SerialName("is_encoded")
    val isEncoded: Int? = null,
    @SerialName("is_ugc")
    val is_ugc: Int = 0,
    
    @SerialName("is_drm_active")
    var is_drm_active: Int = 0,
    @SerialName("drm_dash_url")
    val drmDashUrl: String? = null,
    @SerialName("drm_cast_receiver")
    val drmCastReceiver: String? = null,
    @SerialName("plain_cast_receiver")
    val plainCastReceiver: String? = null,
    @SerialName("is_ad_active")
    val is_ad_active: Int = 0,
    @SerialName("drm_cid")
    val drmCid: String? = null,
    @SerialName("fcm_event_name")
    val fcmEventName: String? = null,
    @SerialName("fcm_event_is_active")
    val fcm_event_is_active: Int = 0,
    @SerialName("data_source")
    val dataSource: String? = "iptv_programs",
    @SerialName("totalCount")
    var totalCount: Int = 0,
    @SerialName("plain_hls_url_for_url_type")
    var paidPlainHlsUrl: String? = null,
    @SerialName("sign_url_expire")
    var signedUrlExpiryDate: String? =null,
    @SerialName("cdn_type")
    var cdnType: String? = null,
    @SerialName("ads_group")
    var adGroup: String? = null,
    @SerialName("featured_banner_code")
    var bannerEventName: String? = null,
    @SerialName("sign_cookie")
    var signedCookie: String? = null,
    @SerialName("sign_cookie_expire")
    var signedCookieExpiryDate: String? =null,
    
    @SerialName("playlist_name")
    var playlistName: String? = null,
    @SerialName("playlist_description")
    var playlistDescription: String? = null,
    @SerialName("isSelected")
    var isSelected: Boolean? = false,
) :Parcelable {
    
    @SerialName("isApproved")
    val isApproved: Int
        get() = if (is_approved == null || is_approved == 1) 1 else 0
    
    @SerialName("isHorizontal")
    val isHorizontal: Int
        get() = if (is_horizontal == null || is_horizontal == 1) 1 else 0

    @SerialName("isLive")
    val isLive: Boolean
        get() = "LIVE".equals(type, ignoreCase = true)
    
    @SerialName("isLinear")
    val isLinear: Boolean
        get() = "LIVE".equals(type, ignoreCase = true) || "Stingray".equals(type, ignoreCase = true) || "RADIO".equals(type, ignoreCase = true)
    
    @SerialName("isVOD")
    val isVOD: Boolean
        get() = "VOD".equals(type, ignoreCase = true)

    @SerialName("isChannel")
    val isChannel: Boolean
        get() = "CHANNEL".equals(type, ignoreCase = true)

    @SerialName("isStingray")
    val isStingray: Boolean
        get() = "stingray".equals(type, ignoreCase = true)

    @SerialName("isFmRadio")
    val isFmRadio: Boolean
        get() = "RADIO".equals(type, ignoreCase = true)
    
    @SerialName("isFmRadio")
    val isAudioBook: Boolean
        get() = "Audio_Book".equals(type, ignoreCase = true)
    
    @SerialName("isCatchup")
    val isCatchup: Boolean
        get() = "CATCHUP".equals(type, ignoreCase = true)
    
    @SerialName("isBucketUrl")
    val isBucketUrl: Boolean
        get() = isEncoded == 0
    
    @SerialName("isDrmActive")
    val isDrmActive: Boolean
        get() = is_drm_active == 1
    
    @SerialName("isAdActive")
    val isAdActive: Boolean
        get() = is_ad_active == 1
    
    @SerialName("isFcmEventActive")
    val isFcmEventActive: Boolean
        get() = fcm_event_is_active == 1
    
    @IgnoredOnParcel
    @SerialName("isExpired")
    var isExpired: Boolean = false
    
    @IgnoredOnParcel
    @SerialName("isOwner")
    var isOwner: Boolean = false
    
    @IgnoredOnParcel
    @SerialName("isPublic")
    var isPublic: Boolean = false
    
    @IgnoredOnParcel
    @SerialName("isPlaylist")
    var isPlaylist: Boolean = false
    
    @IgnoredOnParcel
    @SerialName("isFromSportsCategory")
    var isFromSportsCategory: Boolean = false
    
    @IgnoredOnParcel
    @SerialName("viewProgress")
    var viewProgress: Long = -1L
    
    fun getContentId(): String {
        return if (isOwner && !isPublic && !isPlaylist && mainTableId != null && mainTableId != "0" && !mainTableId.equals("null", ignoreCase = true)) {
            mainTableId ?: id
        } else if (isOwner && !isPublic && isPlaylist && iptvProgramsId != null && iptvProgramsId != "0" && !iptvProgramsId.equals("null", ignoreCase = true)) {
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
    fun getHlsLink(): String? = hlsLinks?.get(0)?.hlsUrlMobile
    
    fun getDrmUrl(isDataConnection: Boolean) = if (isDataConnection) {
        drmDashUrlExtSd?.firstOrNull()?.urlList()?.randomOrNull()?.takeIf { it.isNotBlank() } ?: drmDashUrlExt?.firstOrNull()?.urlList()?.randomOrNull()?.takeIf { it.isNotBlank() } ?: drmDashUrl?.takeIf { it.isNotBlank() }
    } else {
        drmDashUrlExt?.firstOrNull()?.urlList()?.randomOrNull()?.takeIf { it.isNotBlank() } ?: drmDashUrlExtSd?.firstOrNull()?.urlList()?.randomOrNull()?.takeIf { it.isNotBlank() } ?: drmDashUrl?.takeIf { it.isNotBlank() }
    }

    fun formattedShareCount(): String = Utils.getFormattedViewsText(shareCount.toString())

    @SerialName("isPurchased")
    val isPurchased: Boolean
        get() = (individual_price?.toInt() ?: 0) > 0 && individual_purchase ?: false
    
    @SerialName("isPaidSubscribed")
    val isPaidSubscribed: Boolean
        get() = individual_price?.toInt() == 0 && subscription ?: false

    fun isExpired(serverDate: Date): Boolean {
        return try {
            serverDate.after(Utils.getDate(expireTime))
        } catch (ne: NullPointerException) {
            true
        }
    }
    
    fun durationInSeconds(): Int {
        val time = try {
            val format = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
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