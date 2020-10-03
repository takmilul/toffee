package com.banglalink.toffee.ui.useractivities

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.MyBaseAdapterV2

class ChannelSubscriptionListAdapter(callback: ChannelSubscriptionListItemCallback?):
    BasePagingDataAdapter<ChannelInfo>(callback, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.tab_subscriptions_list_item
    }
}