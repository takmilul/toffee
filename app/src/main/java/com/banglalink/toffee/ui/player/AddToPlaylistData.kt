package com.banglalink.toffee.ui.player

import com.banglalink.toffee.model.ChannelInfo

data class AddToPlaylistData(
    val playlistId: Long,
    val items: List<ChannelInfo>,
    val replaceList: Boolean = true,
    val append: Boolean = false
)