package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaylistPlaybackInfo(
    val playlistId: Int,
    val channelOwnerId: Int,
    val playlistName: String,
    var playlistItemCount: Int,
    var isUserPlaylist: Boolean = false,
    var playIndex: Int = 0,
    var currentItem: ChannelInfo? = null
): Parcelable {
    fun getPlaylistIdLong(): Long = playlistId * 10L + 1L
}