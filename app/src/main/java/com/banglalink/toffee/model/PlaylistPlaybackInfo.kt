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
    var channelInfo: ChannelInfo? = null
): Parcelable