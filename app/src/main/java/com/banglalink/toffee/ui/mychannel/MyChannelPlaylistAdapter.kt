package com.banglalink.toffee.ui.mychannel

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.MyChannelPlaylist

class MyChannelPlaylistAdapter(callback: BaseListItemCallback<MyChannelPlaylist>?): BasePagingDataAdapter<MyChannelPlaylist>(callback, ItemComparator()) {
    
    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_my_channel_playlist
    }
}