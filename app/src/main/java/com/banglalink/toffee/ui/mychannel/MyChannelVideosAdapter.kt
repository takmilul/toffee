package com.banglalink.toffee.ui.mychannel

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.ContentReactionCallback

class MyChannelVideosAdapter(callback: ContentReactionCallback<ChannelInfo>?): BasePagingDataAdapter<ChannelInfo>(callback, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_my_channel_videos
    }
}