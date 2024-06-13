package com.banglalink.toffee.ui.player

import com.banglalink.toffee.model.ChannelInfo
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistItem(
    val playlistId: Long,
    val channelInfo: ChannelInfo
)