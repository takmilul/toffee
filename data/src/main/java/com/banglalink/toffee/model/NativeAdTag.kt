package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class NativeAdTag(
    @SerializedName("isFeedAdActive")
    val isFeedAdActive: Boolean = false,
    @SerializedName("feedAdUnitId")
    val feedAdUnitId: String? = null,
    @SerializedName("feedAdInterval")
    val feedAdInterval: Int = 4,
    
    @SerializedName("isRecommendAdActive")
    val isRecommendedAdActive: Boolean = false,
    @SerializedName("recommendAdUnitId")
    val recommendedAdUnitId: String? = null,
    @SerializedName("recommendAdInterval")
    val recommendedAdInterval: Int = 4,
    
    @SerializedName("isPlaylistAdActive")
    val isPlaylistAdActive: Boolean = false,
    @SerializedName("playlistAdUnitId")
    val playlistAdUnitId: String? = null,
    @SerializedName("playlistAdInterval")
    val playlistAdInterval: Int = 4
)