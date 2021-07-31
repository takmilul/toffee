package com.banglalink.toffee.model

import android.os.Parcelable
import android.text.Spanned
import android.util.Base64
import androidx.core.text.HtmlCompat
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.ui.player.HlsLinks
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.Utils.discardZeroFromDuration
import com.banglalink.toffee.util.UtilsKt
import com.banglalink.toffee.util.getFormattedViewsText
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class ChannelInfo(
    var id: String,
    var program_name: String? = null,
    var video_share_url: String? = null,
    var video_trailer_url: String? = null,
    var description: String? = null,
    var water_mark_url: String? = null,
    var type: String? = null,
    var view_count: String? = null,
    var lcn: String? = null,
    var individual_price: String? = null,
    var video_tags: String? = null,
    var duration: String? = null,
    var age_restriction: String? = null,
    var service_operator_id: String? = null,
    var logo_mobile_url: String? = null,
    var poster_url_mobile: String? = null,
    var subscription:Boolean = false,
    var individual_purchase: Boolean = false,
    var expireTime: String? = null,
    var hlsLinks: List<HlsLinks>? = null,
    var channel_logo: String? = null,
    var category: String? = null,
    var subCategory: String? = null,
    var categoryId: Int = 0,
    var subCategoryId: Int = 0,
    var favorite: String? = null,
    var potrait_ratio_800_1200: String? = null,
    var landscape_ratio_1280_720: String? = null,
    var feature_image: String? = null,
    var content_provider_name: String? = null,
    var content_provider_id: String? = null,
    val channel_owner_id: Int = 0,
    var isSubscribed: Int = 0,
    var subscriberCount: Int = 0,

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

    var is_available: Int = 0,
    var reaction: ReactionStatus? = null,   //individual reaction count from server
    var myReaction: Int = Reaction.None.value, //enum value (Reaction.Like.value) etc...
    var shareCount: Long = 0L,
    @SerializedName("playlist_content_id")
    val playlistContentId: Int = 0,
    @SerializedName("channel_profile_url")
    val channelProfileUrl: String? = null,
    @SerializedName("url_type")
    val urlType: Int = 0,
    @SerializedName("is_approved")
    val is_approved: Int? = null,
    val created_at: String? = null,
    @SerializedName("is_horizontal")
    val is_horizontal: Int? = null,
    @SerializedName("landscape_feature_1280_720")
    val ugcFeaturedImage: String? = null,
    @SerializedName("is_encoded")
    val isEncoded: Int? = null,
    @SerializedName("is_ugc")
    val is_ugc: Int = 0
) :Parcelable
{
    val isApproved: Int
        get() = if (is_approved == null || is_approved == 1) 1 else 0
    
    val isHorizontal: Int
        get() = if (is_horizontal == null || is_horizontal == 1) 1 else 0

    val isLive: Boolean
        get() = "LIVE".equals(type, ignoreCase = true)
    val isVOD: Boolean
        get() = "VOD".equals(type, ignoreCase = true)
    val isCatchup: Boolean
        get() = "CATCHUP".equals(type, ignoreCase = true)
    val isBucketUrl: Boolean
        get() = isEncoded == 0
    
    fun getCategory(category: String, subCategory: String): String {
        var itemCategory = "Channels>$category"
        if (subCategory.isNotEmpty()) {
            itemCategory += ">$subCategory"
        }
        return itemCategory
    }

    var viewProgress: Long = -1L
    fun viewProgressPercent(): Int {
        val durationInt = UtilsKt.getLongDuration(duration)
        if(viewProgress < 0L || durationInt <= 0L || isLive) return 0
        if(viewProgress > durationInt) return 1000
        return ((viewProgress.toDouble() / durationInt) * 1000L).toInt()
    }

    fun getDescriptionDecoded(): Spanned? {
        return try {
            HtmlCompat.fromHtml(String(Base64.decode(description, Base64.NO_WRAP)), HtmlCompat.FROM_HTML_MODE_LEGACY)
        } catch (ex: Exception) {
            null
        }
    }

    fun getHlsLink(): String? = hlsLinks?.get(0)?.hls_url_mobile

    fun formattedShareCount(): String = Utils.getFormattedViewsText(shareCount.toString())

    val isPurchased: Boolean
        get() = individual_price?.toInt() ?: 0 > 0 && individual_purchase
    val isPaidSubscribed: Boolean
        get() = individual_price?.toInt() == 0 && subscription

    fun isExpired(serverDate: Date): Boolean {
        return try {
            serverDate.after(Utils.getDate(expireTime))
        } catch (ne: NullPointerException) {
            true
        }
    }

    fun formattedViewCount(): String = getFormattedViewsText(view_count)
    fun formattedDuration(): String = discardZeroFromDuration(duration)
    fun formattedCreateTime(): String = if(!created_at.isNullOrBlank()) Utils.getDateDiffInDayOrHourOrMinute(Utils.getDate(created_at).time) else "0"
    fun formattedSubscriberCount(): String = getFormattedViewsText(subscriberCount.toString())
}