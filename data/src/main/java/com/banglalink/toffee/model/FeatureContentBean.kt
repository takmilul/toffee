package com.banglalink.toffee.model

import com.banglalink.toffee.enums.PageType
import com.google.gson.annotations.SerializedName

data class FeatureContentBean(
    @SerializedName(value = "channels", alternate = ["channelInfo"])
    val channels: List<ChannelInfo>?,
    @SerializedName("subcategory")
    val subcategories: List<SubCategory>?,
    @SerializedName("hashTags")
    val hashTags: List<String>? = null,
    @SerializedName("followers")
    val followers: Long,
    @SerializedName("isFollowed")
    val isFollowed: Int = 0,
    @SerializedName("count")
    val count: Int,
    @SerializedName("totalCount")
    val totalCount: Int = 0,
    @SerializedName("systemTime")
    val systemTime: String? = null,
    @SerializedName("featureType")
    val featureType: Int? = null
) {
    @get:SerializedName("pageType")
    val pageType: PageType
        get() {
            return when(featureType) {
                PageType.Landing.value -> PageType.Landing
                PageType.Category.value -> PageType.Category
                PageType.Channel.value -> PageType.Channel
                else -> PageType.Landing
            }
        }
}