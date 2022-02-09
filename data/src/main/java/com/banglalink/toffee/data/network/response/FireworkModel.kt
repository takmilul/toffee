package com.banglalink.toffee.data.network.response
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
) {
    @get:SerializedName("isActive")
    val isActive: Boolean
        get() = isEnabled ==1
}
