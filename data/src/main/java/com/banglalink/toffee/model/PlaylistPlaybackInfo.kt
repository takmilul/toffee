package com.banglalink.toffee.model

import android.os.Parcelable
import com.banglalink.toffee.enums.PlaylistType
import com.banglalink.toffee.enums.PlaylistType.My_Channel_Playlist
import com.google.android.gms.common.annotation.KeepName
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@KeepName
@Parcelize
data class PlaylistPlaybackInfo(
    @SerializedName("playlistId")
    val playlistId: Int,
    @SerializedName("channelOwnerId")
    val channelOwnerId: Int,
    @SerializedName("playlistName")
    val playlistName: String,
    @SerializedName("playlistItemCount")
    var playlistItemCount: Int,
    @SerializedName("playlist_share_url")
    val playlistShareUrl: String? = null,
    @SerializedName("is_approved")
    val isApproved: Int = 0,
    @SerializedName("isUserPlaylist")
    var playlistType: PlaylistType = My_Channel_Playlist,
    @SerializedName("playIndex")
    var playIndex: Int = 0,
    @SerializedName("currentItem")
    var currentItem: ChannelInfo? = null,
    @SerializedName("isOwner")
    var isOwner: Int = 0,
    @SerializedName("isFromShare")
    var isFromShare: Boolean = false,
    @SerializedName("playlistThumbnail")
    var playlistThumbnail: String? = null,
): Parcelable {
    fun getPlaylistIdLong(): Long = playlistId * 10L + 1L
}