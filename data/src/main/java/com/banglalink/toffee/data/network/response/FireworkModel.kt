package com.banglalink.toffee.data.network.response
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FireworkModel(
    @SerialName("playlist_name")
    val playlistName: String? = null,
    @SerialName("channel_id")
    val channelId: String? = null,
    @SerialName("playlist_id")
    val playlistId: String? = null,
    @SerialName("is_active")
    val isEnabled: Int = 0
) {
//    @get:SerializedName("isActive")
    val isActive: Boolean
        get() = isEnabled == 1
}
