package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class NativeAdTag(
    @SerializedName("isFeedAdActive")
    val isFeedAdActive: Boolean = true,
    @SerializedName("feedAdUnitId")
    val feedAdUnitId: List<String>? = listOf("/21622890900,22419763167/BD_Toffee_Android_Toffeefeed_NativeAdvance_Mid_Fluid"),
    @SerializedName("feedAdInterval")
    val feedAdInterval: Int = 4,
    
    @SerializedName("isRecommendAdActive")
    val isRecommendedAdActive: Boolean = true,
    @SerializedName("recommendAdUnitId")
    val recommendedAdUnitId: List<String>? = listOf("/21622890900,22419763167/BD_Toffee_Android_RecommendVideo_NativeAdvance_Mid_Fluid"),
    @SerializedName("recommendAdInterval")
    val recommendedAdInterval: Int = 4,
    
    @SerializedName("isPlaylistAdActive")
    val isPlaylistAdActive: Boolean = true,
    @SerializedName("playlistAdUnitId")
    val playlistAdUnitId: List<String>? = listOf("/21622890900,22419763167/BD_Toffee_Android_RecommendVideo_NativeAdvance_Mid_Fluid"),
    @SerializedName("playlistAdInterval")
    val playlistAdInterval: Int = 4
)