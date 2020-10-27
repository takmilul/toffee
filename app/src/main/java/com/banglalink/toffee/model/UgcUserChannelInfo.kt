package com.banglalink.toffee.model

import com.banglalink.toffee.util.Utils
import com.google.gson.annotations.SerializedName

data class UgcUserChannelInfo(
    val id: Long,

    @SerializedName("channel_id")
    val channelId: Long,

    @SerializedName("category_id")
    val categoryId: Long,

    @SerializedName("subscriber_count")
    val subscriberCount: Long,

    @SerializedName("is_active")
    val isActive: Int,

    @SerializedName("content_provider_name")
    val contentProviderName: String,

    @SerializedName("content_provider_id")
    val contentProviderId: Long,

    @SerializedName("profile_url")
    val profileUrl: String?,

    @SerializedName("banner_url")
    val bannerUrl: String?,

    val isSubscribed: Int,
    
    @SerializedName("user_id")
    val userId: Int,
) {
    fun getFormatedSubscriberCount() = Utils.getFormattedViewsText(subscriberCount.toString())
}