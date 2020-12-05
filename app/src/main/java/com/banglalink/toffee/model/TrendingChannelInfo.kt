package com.banglalink.toffee.model

import com.banglalink.toffee.util.Utils
import com.google.gson.annotations.SerializedName

data class TrendingChannelInfo(
    val id: Long,
    val subscriberCount: Long,

    @SerializedName("category_id")
    val categoryId: Long,
    
    @SerializedName("is_active")
    val isActive: Int,

    @SerializedName("content_provider_name")
    val contentProviderName: String,

    @SerializedName("content_provider_id")
    val contentProviderId: Long,

    @SerializedName("channel_owner_id")
    val channelOwnerId: Int,
    
    @SerializedName("profile_url")
    val profileUrl: String?,

    @SerializedName("banner_url")
    val bannerUrl: String?,

    val isSubscribed: Int,
    
    @SerializedName("user_id")
    val userId: Int,
) {
    val formatedSubscriberCount = Utils.getFormattedViewsText(subscriberCount.toString())
}
