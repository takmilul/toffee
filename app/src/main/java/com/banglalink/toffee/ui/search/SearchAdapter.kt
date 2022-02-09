package com.banglalink.toffee.ui.search

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.model.ChannelInfo

class SearchAdapter(listener: ProviderIconCallback<ChannelInfo>) : BasePagingDataAdapter<ChannelInfo>(listener, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        if (getItem(position)!!.isLinear) {
            return R.layout.list_item_live_new
        }
        return R.layout.list_item_relative
    }
}