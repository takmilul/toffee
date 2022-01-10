package com.banglalink.toffee.data.network.response
import com.banglalink.toffee.model.FeatureContentBean
import com.banglalink.toffee.model.FollowCategoryBean
import com.google.gson.annotations.SerializedName
data class FireworkModel(
    @SerializedName("playlist_name")
    val playlistName: String?,
    @SerializedName("channel_id")
    val channelId: String?,
    @SerializedName("playlist_id")
    val playlistId: String?,
    @SerializedName("is_active")
    val isEnabled: Int = 0
)
