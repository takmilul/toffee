package com.banglalink.toffee.model

import com.banglalink.toffee.enums.PageType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeatureContentBean(
    @SerialName(value = "channels"/*, alternate = ["channelInfo"]*/)
    val channels: List<ChannelInfo>?,
    @SerialName("subcategory")
    val subcategories: List<SubCategory>?,
    @SerialName("hashTags")
    val hashTags: List<String>? = null,
    @SerialName("followers")
    val followers: Long,
    @SerialName("isFollowed")
    val isFollowed: Int = 0,
    @SerialName("count")
    val count: Int,
    @SerialName("totalCount")
    val totalCount: Int = 0,
    @SerialName("systemTime")
    val systemTime: String? = null,
    @SerialName("featureType")
    val featureType: Int? = null
) {
//    @get:SerializedName("pageType")
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