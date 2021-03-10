package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class FeatureContentBean(
    @SerializedName(value = "channels", alternate = ["channelInfo"])
    val channels: List<ChannelInfo>?,

    @SerializedName("subcategory")
    val subcategories: List<SubCategory>?,
    val hashTags: String? = null,
    val followers: Long,
    val isFollowed: Int = 0,
    val count: Int,
    val totalCount: Int = 0,
    val systemTime: String?=null
)