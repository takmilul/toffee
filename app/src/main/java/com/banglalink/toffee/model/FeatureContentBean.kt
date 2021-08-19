package com.banglalink.toffee.model

import com.banglalink.toffee.enums.PageType
import com.google.gson.annotations.SerializedName

data class FeatureContentBean(
    @SerializedName(value = "channels", alternate = ["channelInfo"])
    val channels: List<ChannelInfo>?,

    @SerializedName("subcategory")
    val subcategories: List<SubCategory>?,
    val hashTags: List<String>? = null,
    val followers: Long,
    val isFollowed: Int = 0,
    val count: Int,
    val totalCount: Int = 0,
    val systemTime: String? = null,
    val featureType: Int? = null
) {
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