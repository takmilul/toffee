package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.android.gms.common.annotation.KeepName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@KeepName
@Parcelize
@Serializable
data class PlaylistPlaybackInfo(
    @SerialName("playlistId")
    val playlistId: Int,
    @SerialName("channelOwnerId")
    val channelOwnerId: Int,
    @SerialName("playlistName")
    val playlistName: String,
    @SerialName("playlistItemCount")
    var playlistItemCount: Int,
    @SerialName("playlist_share_url")
    val playlistShareUrl: String? = null,
    @SerialName("is_approved")
    val isApproved: Int = 0,
    @SerialName("isUserPlaylist")
    var isUserPlaylist: Boolean = false,
    @SerialName("playIndex")
    var playIndex: Int = 0,
    @SerialName("currentItem")
    var currentItem: ChannelInfo? = null,
    @SerialName("isOwner")
    var isOwner: Int = 0,
    @SerialName("isFromShare")
    var isFromShare: Boolean = false
): Parcelable {
    fun getPlaylistIdLong(): Long = playlistId * 10L + 1L
}