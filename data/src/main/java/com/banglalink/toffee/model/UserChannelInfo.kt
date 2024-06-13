package com.banglalink.toffee.model

import com.banglalink.toffee.util.Utils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserChannelInfo(
    @SerialName("id")
    val id: Long = 0L,
    @SerialName("category_id")
    val categoryId: Long = 0L,
    @SerialName("subscriber_count")
    var subscriberCount: Long = 0L,
    @SerialName("is_active")
    val isActive: Int = 0,
    @SerialName("content_provider_name")
    val contentProviderName: String = "",
    @SerialName("content_provider_id")
    val contentProviderId: Long = 0L,
    @SerialName("channel_owner_id")
    val channelOwnerId: Int = 0,
    @SerialName("profile_url")
    val profileUrl: String? = null,
    @SerialName("banner_url")
    val bannerUrl: String? = null,
    @SerialName("isSubscribed")
    var isSubscribed: Int = 0,
    @SerialName("created_at")
    val created_at: String? = null,
    @SerialName("user_id")
    val userId: Int = 0,
) {
    fun formattedSubscriberCount(): String = Utils.getFormattedViewsText(subscriberCount.toString())
    fun formattedCreateTime(): String = if (!created_at.isNullOrBlank()) Utils.getDateDiffInDayOrHourOrMinute(Utils.getDate(created_at).time) else "0"
}