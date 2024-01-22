package com.banglalink.toffee.ui.player

import com.banglalink.toffee.model.ChannelInfo
import kotlinx.serialization.Serializable

@Serializable
data class AddToPlaylistData(
    val playlistId: Long,
    val items: List<ChannelInfo>,
    val replaceList: Boolean = true,
    val append: Boolean = false
)