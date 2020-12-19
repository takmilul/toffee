package com.banglalink.toffee.ui.category.drama

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.ChannelInfo

class EpisodeListAdapter(callback: BaseListItemCallback<ChannelInfo>?):
    BasePagingDataAdapter<ChannelInfo>(callback, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_episode_list
    }
}