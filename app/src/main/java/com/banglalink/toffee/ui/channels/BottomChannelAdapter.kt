package com.banglalink.toffee.ui.channels

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.data.database.entities.TVChannelItem

class BottomChannelAdapter(cb: BaseListItemCallback<TVChannelItem>):
    BasePagingDataAdapter<TVChannelItem>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.fragment_bottom_channel_item_layout
    }
}