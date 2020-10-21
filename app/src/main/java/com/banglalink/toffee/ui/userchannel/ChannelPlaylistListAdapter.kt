package com.banglalink.toffee.ui.userchannel

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.UgcChannelPlaylist

class ChannelPlaylistListAdapter(callback: BaseListItemCallback<UgcChannelPlaylist>?): BasePagingDataAdapter<UgcChannelPlaylist>(callback, ItemComparator()) {
    
    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_channel_playlist
    }
}