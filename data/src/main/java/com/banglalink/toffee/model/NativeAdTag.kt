package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class NativeAdTag(

    @SerializedName("isFeedAdActive")
    val isFeedAdActive: Boolean = true,
    @SerializedName("feedAdUnitId")
    val feedAdUnitId: List<String>? = null,
    @SerializedName("feedAdInterval")
    val feedAdInterval: Int = 4,

    @SerializedName("isRecommendAdActive")
    val isRecommendAdActive: Boolean = true,
    @SerializedName("recommendAdUnitId")
    val recommendAdUnitId: List<String>? = null,
    @SerializedName("recommendAdInterval")
    val recommendAdInterval: Int = 4,

    @SerializedName("isPlaylistAdActive")
    val isPlaylistAdActive: Boolean = true,
    @SerializedName("playlistAdUnitId")
    val playlistAdUnitId: List<String>? = null,
    @SerializedName("playlistAdInterval")
    val playlistAdInterval: Int = 4


)