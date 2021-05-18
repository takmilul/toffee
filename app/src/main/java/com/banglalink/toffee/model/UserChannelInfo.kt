package com.banglalink.toffee.model

import com.banglalink.toffee.util.Utils
import com.google.gson.annotations.SerializedName

data class UserChannelInfo(
    val id: Long = 0L,

    @SerializedName("category_id")
    val categoryId: Long = 0L,

    @SerializedName("subscriber_count")
    var subscriberCount: Long = 0L,

    @SerializedName("is_active")
    val isActive: Int = 0,

    @SerializedName("content_provider_name")
    val contentProviderName: String = "",

    @SerializedName("content_provider_id")
    val contentProviderId: Long = 0L,

    @SerializedName("channel_owner_id")
    val channelOwnerId: Int = 0,
    
    @SerializedName("profile_url")
    val profileUrl: String? = null,

    @SerializedName("banner_url")
    val bannerUrl: String? = null,

    var isSubscribed: Int = 0,
    val created_at: String? = null,
    
    @SerializedName("user_id")
    val userId: Int = 0,
) {
    fun getFormatedSubscriberCount() = Utils.getFormattedViewsText(subscriberCount.toString())
    fun formattedCreateTime(): String = if(!created_at.isNullOrBlank()) Utils.getDateDiffInDayOrHourOrMinute(Utils.getDate(created_at).time) else "0"
}