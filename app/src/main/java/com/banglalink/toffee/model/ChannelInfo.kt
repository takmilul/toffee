package com.banglalink.toffee.model

import android.os.Parcelable
import android.text.Html
import android.text.Spanned
import android.text.SpannedString
import android.util.Base64
import androidx.core.text.HtmlCompat
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.ui.player.HlsLinks
import com.banglalink.toffee.util.Utils
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by MD.TAUFIQUR RAHMAN on 11/22/2016.
 */

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
    var formatted_view_count: String? = null,
    var lcn: String? = null,
    var individual_price: String? = null,
    var video_tags: String? = null,
    var duration: String? = null,
    var formattedDuration: String? = null,
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
    val isApproved: Int? = null,
    val created_at: String? = null,
    var formattedCreateTime: String? = null,
    var formattedSubscriberCount: String? = null,
) :Parcelable
{

    val isLive: Boolean
        get() = "LIVE".equals(type, ignoreCase = true)
    val isVOD: Boolean
        get() = "VOD".equals(type, ignoreCase = true)
    val isCatchup: Boolean
        get() = "CATCHUP".equals(type, ignoreCase = true)

    fun getCategory(category: String, subCategory: String): String {
        var itemCategory = "Channels>$category"
        if (subCategory.isNotEmpty()) {
            itemCategory += ">$subCategory"
        }
        return itemCategory
    }

    fun getDescriptionDecoded(): Spanned? {
        return try {
            HtmlCompat.fromHtml(String(Base64.decode(description, Base64.NO_WRAP)), HtmlCompat.FROM_HTML_MODE_LEGACY)
        } catch (ex: Exception) {
            null
        }
    }

    fun getHlsLink() = hlsLinks!![0].hls_url_mobile

    fun formatedShareCount() = Utils.getFormattedViewsText(shareCount.toString())

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

    companion object {
        fun getHlsArrayList(links: List<String?>?): List<HlsLinks> {
            val hlsLinks: MutableList<HlsLinks> = ArrayList()
            if (links != null) {
                for (link in links) {
                    val hlsLink = HlsLinks()
                    hlsLink.hls_url_mobile = link
                    hlsLinks.add(hlsLink)
                }
            }
            return hlsLinks
        }

        fun getStringArrayList(hlsLinks: List<HlsLinks>?): List<String> {
            val list: MutableList<String> = ArrayList()
            if (hlsLinks != null) {
                for (hls in hlsLinks) {
                    list.add(hls.hls_url_mobile)
                }
            }
            return list
        }
    }
}