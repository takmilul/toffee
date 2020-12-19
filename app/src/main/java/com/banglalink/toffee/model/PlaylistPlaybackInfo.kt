package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlaylistPlaybackInfo(
    val playlistId: Int,
    val channelOwnerId: Int,
    val isOwner: Int,
    val playlistName: String,
    val playlistItemCount: Int,
    var playIndex: Int = 0,
    var currentItem: ChannelInfo? = null
): Parcelable {
    fun getPlaylistIdLong(): Long = playlistId * 10L + 1L
}