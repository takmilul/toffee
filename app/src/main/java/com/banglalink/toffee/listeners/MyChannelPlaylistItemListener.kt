package com.banglalink.toffee.listeners

import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.ContentReactionCallback

interface MyChannelPlaylistItemListener: ContentReactionCallback<ChannelInfo> {
    fun onItemClickAtPosition(position: Int, item: ChannelInfo)
}