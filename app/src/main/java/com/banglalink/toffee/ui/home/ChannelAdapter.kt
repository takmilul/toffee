package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.ChannelInfo
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter

class ChannelAdapter(cb: BaseListItemCallback<ChannelInfo>):
    BasePagingDataAdapter<ChannelInfo>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.live_tv_item_new
    }
}